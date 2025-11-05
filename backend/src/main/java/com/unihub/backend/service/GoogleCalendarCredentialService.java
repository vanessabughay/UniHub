package com.unihub.backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.unihub.backend.model.GoogleCalendarCredential;
import com.unihub.backend.repository.GoogleCalendarCredentialRepository;
import com.unihub.backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Optional;

@Service
public class GoogleCalendarCredentialService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarCredentialService.class);

    private final GoogleCalendarCredentialRepository credentialRepository;
    private final UsuarioRepository usuarioRepository;
    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    @Value("${google.oauth.client-id}")
    private String clientId;

    @Value("${google.oauth.client-secret:}")
    private String clientSecret;

    @Value("${google.oauth.calendar-redirect-uri:}")
    private String redirectUri;

    @Value("${google.calendar.application-name:UniHub}")
    private String applicationName;

    public GoogleCalendarCredentialService(GoogleCalendarCredentialRepository credentialRepository,
                                           UsuarioRepository usuarioRepository) {
        this.credentialRepository = credentialRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public GoogleCalendarCredential link(Long usuarioId, String authCode) {
        if (authCode == null || authCode.isBlank()) {
            throw new IllegalArgumentException("Authorization code do Google é obrigatório");
        }
        try {
            GoogleAuthorizationCodeTokenRequest tokenRequest = new GoogleAuthorizationCodeTokenRequest(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    jsonFactory,
                    "https://oauth2.googleapis.com/token",
                    clientId,
                    clientSecret,
                    authCode,
                    redirectUri
            );
            GoogleTokenResponse response = tokenRequest.execute();

            GoogleCalendarCredential credential = credentialRepository.findByUsuarioId(usuarioId)
                    .orElseGet(() -> {
                        GoogleCalendarCredential novo = new GoogleCalendarCredential();
                        novo.setUsuario(usuarioRepository.getReferenceById(usuarioId));
                        return novo;
                    });

            credential.setAccessToken(response.getAccessToken());
            String refreshToken = response.getRefreshToken();
            if (refreshToken != null && !refreshToken.isBlank()) {
                credential.setRefreshToken(refreshToken);
            } else if (credential.getRefreshToken() == null || credential.getRefreshToken().isBlank()) {
                throw new IllegalStateException("O Google não retornou um refresh token. Solicite novamente a autorização permitindo acesso offline.");
            }

            Long expiresInSeconds = response.getExpiresInSeconds();
            Instant expiry = expiresInSeconds != null
                    ? Instant.now().plusSeconds(expiresInSeconds)
                    : Instant.now().plusSeconds(3600);
            credential.setAccessTokenExpiresAt(expiry);
            credential.setLastSyncedAt(null);

            return credentialRepository.save(credential);
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Falha ao trocar o authorization code por tokens do Google: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void unlink(Long usuarioId) {
        credentialRepository.deleteByUsuarioId(usuarioId);
    }

    public boolean isLinked(Long usuarioId) {
        return credentialRepository.existsByUsuarioId(usuarioId);
    }

    public Optional<GoogleCalendarCredential> findByUsuario(Long usuarioId) {
        return credentialRepository.findByUsuarioId(usuarioId);
    }

    public Optional<CalendarClient> prepareCalendar(Long usuarioId) {
        return credentialRepository.findByUsuarioId(usuarioId).flatMap(credential -> {
            try {
                var transport = GoogleNetHttpTransport.newTrustedTransport();
                GoogleCredential googleCredential = new GoogleCredential.Builder()
                        .setTransport(transport)
                        .setJsonFactory(jsonFactory)
                        .setClientSecrets(clientId, clientSecret)
                        .build();

                applyTokens(credential, googleCredential);

                Calendar calendar = new Calendar.Builder(transport, jsonFactory, googleCredential)
                        .setApplicationName(applicationName)
                        .build();

                return Optional.of(new CalendarClient(calendar, credential));
            } catch (Exception e) {
                logger.warn("Falha ao preparar cliente do Google Calendar para usuário {}", usuarioId, e);
                return Optional.empty();
            }
        });
    }

    private void applyTokens(GoogleCalendarCredential credential, GoogleCredential googleCredential) throws IOException {
        googleCredential.setAccessToken(credential.getAccessToken());
        googleCredential.setRefreshToken(credential.getRefreshToken());
        if (credential.getAccessTokenExpiresAt() != null) {
            googleCredential.setExpirationTimeMilliseconds(credential.getAccessTokenExpiresAt().toEpochMilli());
        }

        boolean needsRefresh = credential.getAccessTokenExpiresAt() == null
                || credential.getAccessTokenExpiresAt().isBefore(Instant.now().plusSeconds(60));
        if (needsRefresh) {
            if (credential.getRefreshToken() == null || credential.getRefreshToken().isBlank()) {
                throw new IllegalStateException("Refresh token ausente para o usuário " + credential.getUsuario().getId());
            }
            if (!googleCredential.refreshToken()) {
                throw new IllegalStateException("Falha ao atualizar o access token do Google Calendar");
            }
            credential.setAccessToken(googleCredential.getAccessToken());
            Long expiresMs = googleCredential.getExpirationTimeMilliseconds();
            if (expiresMs != null) {
                credential.setAccessTokenExpiresAt(Instant.ofEpochMilli(expiresMs));
            } else {
                credential.setAccessTokenExpiresAt(Instant.now().plusSeconds(3600));
            }
            credentialRepository.save(credential);
        }
    }

    public record CalendarClient(Calendar calendar, GoogleCalendarCredential credential) {}
}