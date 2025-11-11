package com.unihub.backend.dto.compartilhamento;

import com.unihub.backend.model.Notificacao;

import java.time.LocalDateTime;

public class NotificacaoResponse {

    private Long id;
    private String titulo;
    private String mensagem;
    private boolean lida;
    private String tipo;
     private String categoria;
    private Long conviteId;
    private Long referenciaId;
    private boolean interacaoPendente;
    private String metadataJson;
    private LocalDateTime criadaEm;
    private LocalDateTime atualizadaEm;

    public static NotificacaoResponse fromEntity(Notificacao notificacao) {
        NotificacaoResponse response = new NotificacaoResponse();
        response.setId(notificacao.getId());
        response.setTitulo(notificacao.getTitulo());
        response.setMensagem(notificacao.getMensagem());
        response.setLida(notificacao.isLida());
        response.setTipo(notificacao.getTipo());
        response.setCategoria(notificacao.getCategoria());
        response.setConviteId(notificacao.getConvite() != null ? notificacao.getConvite().getId() : null);
        response.setReferenciaId(notificacao.getReferenciaId());
        response.setInteracaoPendente(notificacao.isInteracaoPendente());
        response.setMetadataJson(notificacao.getMetadataJson());
        response.setCriadaEm(notificacao.getCriadaEm());
        response.setAtualizadaEm(notificacao.getAtualizadaEm());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Long getConviteId() {
        return conviteId;
    }

    public void setConviteId(Long conviteId) {
        this.conviteId = conviteId;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(Long referenciaId) {
        this.referenciaId = referenciaId;
    }

    public boolean isInteracaoPendente() {
        return interacaoPendente;
    }

    public void setInteracaoPendente(boolean interacaoPendente) {
        this.interacaoPendente = interacaoPendente;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    
    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    public void setAtualizadaEm(LocalDateTime atualizadaEm) {
        this.atualizadaEm = atualizadaEm;
    }
}