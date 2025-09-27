package com.unihub.backend.service;

import com.unihub.backend.dto.ForgotPasswordRequest;
import com.unihub.backend.dto.ResetPasswordRequest;
import com.unihub.backend.model.PasswordResetToken;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.PasswordResetTokenRepository;
import com.unihub.backend.repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UsuarioRepository usuarioRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    /** Remetente: deve ser o mesmo do Gmail configurado em spring.mail.username */
    @Value("${spring.mail.username}")
    private String from;

    /**
     * Base para montar o link de reset. Pode ser um deep-link do app
     * (ex.: unihub://reset) ou uma URL web (ex.: https://app.unihub.com/reset).
     * Se não definido via env, cai no deep-link padrão abaixo.
     */
    @Value("${app.reset.base-uri:unihub://reset}")
    private String resetBaseUri;

    public PasswordResetService(UsuarioRepository usuarioRepo,
                                PasswordResetTokenRepository tokenRepo,
                                PasswordEncoder passwordEncoder,
                                JavaMailSender mailSender) {
        this.usuarioRepo = usuarioRepo;
        this.tokenRepo = tokenRepo;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @Transactional
    public void requestReset(ForgotPasswordRequest req) {
        // 1) Busca usuário por e-mail. Se não existir, não vaza informação (retorna 200 OK sem ação)
        Optional<Usuario> userOpt = usuarioRepo.findByEmail(req.getEmail());
        if (userOpt.isEmpty()) return;

        Usuario user = userOpt.get();

        // 2) Gera token (texto puro) + salva apenas o HASH (segurança)
        String rawToken = UUID.randomUUID().toString().replace("-", "");
        String hash = sha256(rawToken);

        PasswordResetToken prt = new PasswordResetToken();
        prt.setUserId(user.getId());
        prt.setTokenHash(hash);
        prt.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
        tokenRepo.save(prt);

        // 3) Monta o link (deep-link ou web) e envia e-mail
        String link = String.format("%s?token=%s", resetBaseUri, rawToken);
        enviarEmailReset(user.getEmail(), link);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        // 1) Valida token (via hash)
        String hash = sha256(req.getToken());
        PasswordResetToken prt = tokenRepo.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido"));

        if (prt.isUsed() || prt.isExpired()) {
            throw new IllegalStateException("Token expirado ou já utilizado");
        }

        // 2) Atualiza senha do usuário
        Usuario user = usuarioRepo.findById(prt.getUserId())
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado"));

        user.setSenha(passwordEncoder.encode(req.getNewPassword()));
        usuarioRepo.save(user);

        // 3) Marca token como usado
        prt.setUsedAt(Instant.now());
        tokenRepo.save(prt);
    }

    /* ================== Helpers ================== */

    private void enviarEmailReset(String to, String link) {
        try {
            var msg = mailSender.createMimeMessage();
            var helper = new org.springframework.mail.javamail.MimeMessageHelper(msg, "UTF-8");
            helper.setTo(to);
            helper.setFrom(from); // <— IMPORTANTE: com Gmail, o from deve ser o mesmo do spring.mail.username
            helper.setSubject("Redefinição de senha - UniHub");
            helper.setText(
                "<p>Para redefinir sua senha, toque no link abaixo no seu dispositivo:</p>" +
                "<p><a href=\"" + link + "\">Redefinir senha</a></p>" +
                "<p>Este link expira em 30 minutos.</p>",
                true
            );
            mailSender.send(msg);
        } catch (Exception e) {
            // Em dev, se SMTP falhar, ainda imprimimos o link no log para testes manuais
            System.out.println("Falha ao enviar e-mail. Link de reset (DEV): " + link);
        }
    }

    private static String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
