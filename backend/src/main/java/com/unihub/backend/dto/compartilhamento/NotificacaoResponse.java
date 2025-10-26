package com.unihub.backend.dto.compartilhamento;

import com.unihub.backend.model.Notificacao;

import java.time.LocalDateTime;

public class NotificacaoResponse {

    private Long id;
    private String mensagem;
    private boolean lida;
    private String tipo;
    private Long conviteId;
    private LocalDateTime criadaEm;

    public static NotificacaoResponse fromEntity(Notificacao notificacao) {
        NotificacaoResponse response = new NotificacaoResponse();
        response.setId(notificacao.getId());
        response.setMensagem(notificacao.getMensagem());
        response.setLida(notificacao.isLida());
        response.setTipo(notificacao.getTipo());
        response.setConviteId(notificacao.getConvite() != null ? notificacao.getConvite().getId() : null);
        response.setCriadaEm(notificacao.getCriadaEm());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public boolean isLida() {
        return lida;
    }

    public void setLida(boolean lida) {
        this.lida = lida;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getConviteId() {
        return conviteId;
    }

    public void setConviteId(Long conviteId) {
        this.conviteId = conviteId;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }
}