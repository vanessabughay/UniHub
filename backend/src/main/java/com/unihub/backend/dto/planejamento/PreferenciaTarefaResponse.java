package com.unihub.backend.dto.planejamento;

public class PreferenciaTarefaResponse {
    
    private boolean receberNotificacoes;

    public PreferenciaTarefaResponse() {
    }

    public PreferenciaTarefaResponse(boolean receberNotificacoes) {
        this.receberNotificacoes = receberNotificacoes;
    }

    public boolean isReceberNotificacoes() {
        return receberNotificacoes;
    }

    public void setReceberNotificacoes(boolean receberNotificacoes) {
        this.receberNotificacoes = receberNotificacoes;
    }
}