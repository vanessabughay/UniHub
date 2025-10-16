package com.unihub.backend.dto.planejamento;

public class PreferenciaComentarioResponse {

    private boolean receberNotificacoes;

    public PreferenciaComentarioResponse() {
    }

    public PreferenciaComentarioResponse(boolean receberNotificacoes) {
        this.receberNotificacoes = receberNotificacoes;
    }

    public boolean isReceberNotificacoes() {
        return receberNotificacoes;
    }

    public void setReceberNotificacoes(boolean receberNotificacoes) {
        this.receberNotificacoes = receberNotificacoes;
    }
}