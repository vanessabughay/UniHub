package com.unihub.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.unihub.backend.model.Contato;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.unihub.backend.repository.InstituicaoRepository;
import com.unihub.backend.dto.LoginResponse;


import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;


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

    // Armazena token -> usuarioId (STATeless de verdade exigiria JWT; aqui mantém seu fluxo atual)
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();

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
            String token = gerarToken(usuario.getId()); // <- usa o novo método
            boolean calendarLinked = googleCalendarCredentialService.isLinked(usuario.getId());
            boolean hasInstitution = !instituicaoRepository.findByUsuarioIdOrderByNomeAsc(usuario.getId()).isEmpty();
            return new LoginResponse(token, usuario.getNomeUsuario(), usuario.getEmail(), usuario.getId(), calendarLinked, hasInstitution);
        }
        return null;
    }

    /** NOVO: usado pelo GoogleAuthService */
    public String gerarToken(Long usuarioId) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, usuarioId);
        return token;
    }

    public Long getUsuarioIdPorToken(String token) {
        return tokens.get(token);
    }

    /** (Opcional) invalidar token, caso queira logout */
    public void invalidarToken(String token) {
        tokens.remove(token);
    }
}
