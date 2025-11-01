package com.unihub.backend.dto.planejamento;

import java.util.List;

public class TarefaPlanejamentoRequest {

    private String titulo;
    private String descricao;
    private String dataPrazo;
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

     public String getDataPrazo() {
        return dataPrazo;
    }

    public void setDataPrazo(String dataPrazo) {
        this.dataPrazo = dataPrazo;
    }

    public List<Long> getResponsavelIds() {
        return responsavelIds;
    }

    public void setResponsavelIds(List<Long> responsavelIds) {
        this.responsavelIds = responsavelIds;
    }
}