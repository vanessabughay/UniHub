package com.unihub.backend.dto.planejamento;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.grammars.hql.HqlParser.LocalDateTimeFunctionContext;

public class TarefaPlanejamentoRequest {

    private String titulo;
    private String descricao;
    private LocalDateTime dataPrazo;
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

    public LocalDateTime getDataPrazo() {
        return dataPrazo;
    }

    public void setDataPrazo(LocalDateTime dataPrazo) {
        this.dataPrazo = dataPrazo;
    }

    public List<Long> getResponsavelIds() {
        return responsavelIds;
    }

    public void setResponsavelIds(List<Long> responsavelIds) {
        this.responsavelIds = responsavelIds;
    }
}