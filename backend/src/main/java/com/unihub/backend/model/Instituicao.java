package com.unihub.backend.model;

import jakarta.persistence.*;

@Entity
public class Instituicao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private Double mediaAprovacao;
    private Integer frequenciaMinima;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Double getMediaAprovacao() {
        return mediaAprovacao;
    }

    public void setMediaAprovacao(Double mediaAprovacao) {
        this.mediaAprovacao = mediaAprovacao;
    }

    public Integer getFrequenciaMinima() {
        return frequenciaMinima;
    }

    public void setFrequenciaMinima(Integer frequenciaMinima) {
        this.frequenciaMinima = frequenciaMinima;
    }
}