package com.unihub.backend.dto.planejamento;

public class AtualizarPreferenciaTarefaRequest {
    
    private boolean receberNotificacoes;

    public boolean isReceberNotificacoes() {
        return receberNotificacoes;
    }

    public void setReceberNotificacoes(boolean receberNotificacoes) {
        this.receberNotificacoes = receberNotificacoes;
    }
}