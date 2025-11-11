package com.unihub.backend.dto.notificacoes;

import java.util.Map;

public class NotificacaoLogRequest {

    private String titulo;
    private String mensagem;
    private String tipo;
    private String categoria;
    private Long referenciaId;
    private Boolean interacaoPendente;
    private Map<String, Object> metadata;
    private Long timestamp;

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

    public Long getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(Long referenciaId) {
        this.referenciaId = referenciaId;
    }

    public Boolean getInteracaoPendente() {
        return interacaoPendente;
    }

    public void setInteracaoPendente(Boolean interacaoPendente) {
        this.interacaoPendente = interacaoPendente;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}