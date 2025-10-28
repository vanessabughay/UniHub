package com.unihub.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class InvitationEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${app.invite.apk-url:}")
    private String apkUrl;

    public InvitationEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarConvite(String emailDestino, String nomeRemetente) {
        if (emailDestino == null || emailDestino.isBlank()) {
            return;
        }

        String emailNormalizado = emailDestino.trim();

        try {
            var mensagem = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensagem, "UTF-8");
            helper.setTo(emailNormalizado);
            helper.setFrom(from);
            helper.setSubject("Convite para o UniHub");

            StringBuilder corpo = new StringBuilder();
            corpo.append("<p>Olá!</p>");
            if (nomeRemetente != null && !nomeRemetente.isBlank()) {
                corpo.append("<p><strong>").append(nomeRemetente)
                        .append("</strong> convidou você para se juntar ao UniHub.</p>");
            } else {
                corpo.append("<p>Você foi convidado para se juntar ao UniHub.</p>");
            }
            corpo.append("<p>Crie sua conta com este e-mail para visualizar e aceitar o pedido de amizade enviado anteriormente.</p>");

            if (apkUrl != null && !apkUrl.isBlank()) {
                corpo.append("<p>Faça o download do aplicativo pelo link abaixo:</p>");
                corpo.append("<p><a href=\"").append(apkUrl)
                        .append("\">Baixar aplicativo UniHub</a></p>");
            }

            corpo.append("<p>Se já possui o aplicativo, basta abrir e finalizar o cadastro utilizando este e-mail.</p>");

            helper.setText(corpo.toString(), true);
            mailSender.send(mensagem);
        } catch (Exception ex) {
            System.out.println("Falha ao enviar e-mail de convite para " + emailNormalizado + ": " + ex.getMessage());
        }
    }
}