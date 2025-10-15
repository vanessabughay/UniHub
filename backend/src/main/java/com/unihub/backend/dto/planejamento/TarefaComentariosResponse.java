package com.unihub.backend.dto.planejamento;

import java.util.List;

public class TarefaComentariosResponse {

    private List<TarefaComentarioResponse> comentarios;
    private boolean receberNotificacoes;

    public List<TarefaComentarioResponse> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<TarefaComentarioResponse> comentarios) {
        this.comentarios = comentarios;
    }

    public boolean isReceberNotificacoes() {
        return receberNotificacoes;
    }

    public void setReceberNotificacoes(boolean receberNotificacoes) {
        this.receberNotificacoes = receberNotificacoes;
    }
}