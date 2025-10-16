package com.unihub.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tarefas_comentarios")
public class TarefaComentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_id", nullable = false)
    private TarefaPlanejamento tarefa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(nullable = false, length = 2000)
    private String conteudo;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.dataCriacao = now;
        this.dataAtualizacao = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TarefaPlanejamento getTarefa() {
        return tarefa;
    }

    public void setTarefa(TarefaPlanejamento tarefa) {
        this.tarefa = tarefa;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}