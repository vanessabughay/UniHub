package com.unihub.backend.dto.planejamento;

import java.util.List;

public class AtualizarTarefaPlanejamentoRequest {

    private String titulo;
    private String descricao;
    private String status;
    private String prazo;
    private List<Long> responsavelIds;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPrazo() {
        return prazo;
    }

    public void setPrazo(String prazo) {
        this.prazo = prazo;
    }

    

    public List<Long> getResponsavelIds() {
        return responsavelIds;
    }

    public void setResponsavelIds(List<Long> responsavelIds) {
        this.responsavelIds = responsavelIds;
    }
}