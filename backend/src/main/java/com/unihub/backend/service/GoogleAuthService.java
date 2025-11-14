package com.unihub.backend.service;

import java.util.Collections;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.unihub.backend.repository.ContatoRepository;
import com.unihub.backend.repository.InstituicaoRepository;
import com.google.api.client.json.gson.GsonFactory;

import com.unihub.backend.dto.LoginResponse;
import com.unihub.backend.model.enums.AuthProvider;
import com.unihub.backend.model.Contato;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.UsuarioRepository;


@Service
public class GoogleAuthService {

    @Value("${google.oauth.client-id}")
    private String googleClientId;

    private final UsuarioRepository usuarioRepository;
    private final AutenticacaoService autenticacaoService;
    private final GoogleCalendarCredentialService googleCalendarCredentialService;
    private final InstituicaoRepository instituicaoRepository;
    private final ContatoService contatoService;

    public GoogleAuthService(UsuarioRepository usuarioRepository,
                             AutenticacaoService autenticacaoService,
                             GoogleCalendarCredentialService googleCalendarCredentialService,
                             InstituicaoRepository instituicaoRepository,
                             ContatoService contatoService) {
        this.usuarioRepository = usuarioRepository;
        this.autenticacaoService = autenticacaoService;
        this.googleCalendarCredentialService = googleCalendarCredentialService;
        this.instituicaoRepository = instituicaoRepository;
        this.contatoService = contatoService;
    }

    public LoginResponse loginWithGoogle(String idTokenString) {
        try {
            var transport = GoogleNetHttpTransport.newTrustedTransport();
            var jsonFactory = GsonFactory.getDefaultInstance();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new RuntimeException("ID Token inválido ou expirado");
            }

            Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            if (email != null) {
                email = email.trim();
            }
            boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String sub = payload.getSubject(); // providerId estável do Google

            // Upsert por e-mail
            Usuario user = email == null ? null : usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
            if (user == null) {
                user = new Usuario();
                user.setEmail(email);
            }
            // Campos básicos
            user.setNomeUsuario(name != null ? name : user.getNomeUsuario());
            user.setLastLoginAt(new Date());

            // Se você adicionou estes campos na entidade, mantenha:
            try {
                user.setProvider(AuthProvider.GOOGLE);
            } catch (Exception ignored) {}
            try {
                user.setProviderId(sub);
            } catch (Exception ignored) {}
            try {
                user.setPictureUrl(pictureUrl);
            } catch (Exception ignored) {}
            try {
                user.setEmailVerified(emailVerified);
            } catch (Exception ignored) {}

            Usuario salvo = usuarioRepository.save(user);

            // Atualiza convites pendentes direcionados a este e-mail, assim como no cadastro nativo
            contatoService.processarConvitesPendentesParaUsuario(salvo);

            // gera jwt
            // Se seu AutenticacaoService gerar token por id:
            String token = autenticacaoService.gerarToken(salvo.getId());

            boolean calendarLinked = googleCalendarCredentialService.isLinked(salvo.getId());
            boolean hasInstitution = !instituicaoRepository.findByUsuarioIdOrderByNomeAsc(salvo.getId()).isEmpty();
            return new LoginResponse(token, salvo.getNomeUsuario(), salvo.getEmail(), salvo.getId(), calendarLinked, hasInstitution);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao validar token do Google: " + e.getMessage(), e);
        }
    }
}
