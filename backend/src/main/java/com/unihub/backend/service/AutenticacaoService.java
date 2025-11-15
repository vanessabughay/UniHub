package com.unihub.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.unihub.backend.model.Contato;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.unihub.backend.repository.InstituicaoRepository;
import com.unihub.backend.dto.LoginResponse;


import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;

import com.unihub.backend.model.AuthToken;
import com.unihub.backend.repository.AuthTokenRepository;


@Service
public class AutenticacaoService {

    @Autowired
    private UsuarioRepository repository;


    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GoogleCalendarCredentialService googleCalendarCredentialService;

    @Autowired
    private ContatoService contatoService;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    // Cache in-memory para acelerar a validação de tokens já conhecidos.
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();

    private static final Duration TOKEN_TTL = Duration.ofDays(7);

    public Usuario registrar(Usuario usuario) {
        String email = Optional.ofNullable(usuario.getEmail())
                .map(String::trim)
                .orElse("");

        if (email.isEmpty()) {
            throw new IllegalArgumentException("E-mail é obrigatório");
        }

        Optional<Usuario> existente = repository.findByEmailIgnoreCase(email);

        if (existente.isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
        usuario.setEmail(email);
        // Se for cadastro nativo, senha vem preenchida; se for social, não chame este método.
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        Usuario salvo = repository.save(usuario);

            contatoService.processarConvitesPendentesParaUsuario(salvo);

        return salvo;
    }

    public LoginResponse login(String email, String senha) {
        Optional<Usuario> usuarioOpt = repository.findByEmail(email);
        if (usuarioOpt.isPresent() && passwordEncoder.matches(senha, usuarioOpt.get().getSenha())) {
            Usuario usuario = usuarioOpt.get();
              String token = gerarToken(usuario.getId());
            boolean calendarLinked = googleCalendarCredentialService.isLinked(usuario.getId());
            boolean hasInstitution = !instituicaoRepository.findByUsuarioIdOrderByNomeAsc(usuario.getId()).isEmpty();
            return new LoginResponse(token, usuario.getNomeUsuario(), usuario.getEmail(), usuario.getId(), calendarLinked, hasInstitution);
        }
        return null;
    }

    /** NOVO: usado pelo GoogleAuthService */
    @Transactional
    public String gerarToken(Long usuarioId) {
         if (usuarioId == null) {
            throw new IllegalArgumentException("Usuário para geração de token é obrigatório");
        }

        LocalDateTime agora = agoraUtc();

        // Limpa tokens expirados antes de seguir.
        authTokenRepository.deleteByExpiraEmBefore(agora);

        // Reutiliza token ativo se existir para o usuário, renovando a validade.
        Optional<AuthToken> existente = authTokenRepository
                .findFirstByUsuarioIdAndExpiraEmAfterOrderByExpiraEmDesc(usuarioId, agora);
        if (existente.isPresent()) {
            AuthToken tokenAtivo = existente.get();
            tokenAtivo.setExpiraEm(agora.plus(TOKEN_TTL));
            AuthToken atualizado = authTokenRepository.save(tokenAtivo);
            cacheToken(atualizado);
            return atualizado.getToken();
        }

        AuthToken novoToken = new AuthToken();
        novoToken.setUsuarioId(usuarioId);
        novoToken.setToken(UUID.randomUUID().toString());
        novoToken.setCriadoEm(agora);
        novoToken.setExpiraEm(agora.plus(TOKEN_TTL));

        AuthToken salvo = authTokenRepository.save(novoToken);
        cacheToken(salvo);
        return salvo.getToken();
    }

    public Long getUsuarioIdPorToken(String token) {
        if (token == null) {
            return null;
        }

        String normalizedToken = token.trim();
        if (normalizedToken.isEmpty()) {
            return null;
        }

        Long cached = tokens.get(normalizedToken);
        if (cached != null) {
            return cached;
        }

        LocalDateTime agora = agoraUtc();

        try {
            Optional<AuthToken> authTokenOpt = authTokenRepository.findByToken(normalizedToken);
            if (authTokenOpt.isEmpty()) {
                return null;
            }

            AuthToken authToken = authTokenOpt.get();
            if (authToken.getExpiraEm().isBefore(agora)) {
                authTokenRepository.deleteByToken(normalizedToken);
                tokens.remove(normalizedToken);
                return null;
            }

            Long usuarioId = authToken.getUsuarioId();
            tokens.put(normalizedToken, usuarioId);
            return usuarioId;
        } catch (DataAccessException ex) {
            return tokens.get(normalizedToken);
        }
    }

    /** (Opcional) invalidar token, caso queira logout */
    public void invalidarToken(String token) {
         if (token == null || token.isBlank()) {
            return;
        }

        String normalizedToken = token.trim();
        tokens.remove(normalizedToken);
        authTokenRepository.deleteByToken(normalizedToken);
    }

    @PostConstruct
    void carregarTokensAtivos() {
        LocalDateTime agora = agoraUtc();
        try {
            List<AuthToken> ativos = authTokenRepository.findByExpiraEmAfter(agora);
            for (AuthToken token : ativos) {
                cacheToken(token);
            }
        } catch (DataAccessException ignored) {
        }
    }

    private void cacheToken(AuthToken token) {
        if (token == null) {
            return;
        }
        String valor = Optional.ofNullable(token.getToken())
                .map(String::trim)
                .orElse(null);
        if (valor == null || valor.isEmpty()) {
            return;
        }
        tokens.put(valor, token.getUsuarioId());
    }

    private LocalDateTime agoraUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
