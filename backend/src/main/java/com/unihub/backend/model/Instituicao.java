package com.unihub.backend.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
public class Instituicao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private Double mediaAprovacao;
    private Integer frequenciaMinima;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @JsonBackReference("usuario-instituicoes")
    private Usuario usuario;

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

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}