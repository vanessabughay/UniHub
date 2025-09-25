package com.unihub.backend.dto.planejamento;

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

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
}