package com.unihub.backend.dto.planejamento;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import com.unihub.backend.model.enums.EstadoPlanejamento;

public class ColunaPlanejamentoRequest {

    private String titulo;
    private EstadoPlanejamento estado;
    private Integer ordem;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public EstadoPlanejamento getEstado() {
        return estado;
    }

    public void setEstado(EstadoPlanejamento estado) {
        this.estado = estado;
    }

    @JsonGetter("status")
    public String getStatus() {
        if (estado == null) {
            return null;
        }
        return estado == EstadoPlanejamento.CONCLUIDO ? "CONCLUIDA" : "INICIADA";
    }

    @JsonSetter("status")
    public void setStatus(String status) {
        if (status == null) {
            this.estado = null;
            return;
        }

        switch (status.toUpperCase()) {
            case "CONCLUIDA":
                this.estado = EstadoPlanejamento.CONCLUIDO;
                break;
            case "EM_ANDAMENTO":
            case "INICIADA":
            default:
                this.estado = EstadoPlanejamento.EM_ANDAMENTO;
                break;
        }
    }
    
    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
}