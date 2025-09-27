package com.unihub.backend.dto.planejamento;

public class AtualizarStatusTarefaRequest {

    private boolean concluida;

    public boolean isConcluida() {
        return concluida;
    }

    public void setConcluida(boolean concluida) {
        this.concluida = concluida;
    }
}