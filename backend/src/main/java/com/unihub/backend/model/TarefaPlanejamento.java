package com.unihub.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.unihub.backend.model.enums.TarefaStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "tarefas_planejamento")
public class TarefaPlanejamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 2000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TarefaStatus status = TarefaStatus.PENDENTE;

    @Column(nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    private LocalDate dataPrazo;

    private LocalDateTime dataConclusao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coluna_id", nullable = false)
    @JsonBackReference("coluna-tarefas")
    private ColunaPlanejamento coluna;

    @ManyToMany
    @JoinTable(
            name = "tarefas_planejamento_responsaveis",
            joinColumns = @JoinColumn(name = "tarefa_id"),
            inverseJoinColumns = @JoinColumn(name = "contato_id")
    )
    @JsonIgnore
    private Set<Contato> responsaveis = new LinkedHashSet<>();

    @Column(name = "responsaveis_ids", length = 1000)
    private String responsaveisIds;

    @OneToMany(mappedBy = "tarefa", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<TarefaComentario> comentarios = new ArrayList<>();

    @OneToMany(mappedBy = "tarefa", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<TarefaComentarioNotificacao> notificacoesComentario = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public TarefaStatus getStatus() {
        return status;
    }

    public void setStatus(TarefaStatus status) {
        this.status = status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDate getDataPrazo() {
        return dataPrazo;
    }

    public void setDataPrazo(LocalDate dataPrazo) {
        this.dataPrazo = dataPrazo;
    }

    public LocalDateTime getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(LocalDateTime dataConclusao) {
        this.dataConclusao = dataConclusao;
    }

    public ColunaPlanejamento getColuna() {
        return coluna;
    }

    public void setColuna(ColunaPlanejamento coluna) {
        this.coluna = coluna;
    }

    public Set<Contato> getResponsaveis() {
        return responsaveis;
    }

    public void setResponsaveis(Set<Contato> responsaveis) {
        if (responsaveis == null) {
            this.responsaveis = new LinkedHashSet<>();
        } else {
            this.responsaveis = responsaveis;
        }
        atualizarResponsaveisIds();
        }

    @JsonProperty("responsaveis")
    public String getResponsaveisIdsRegistrados() {
        return responsaveisIds;
    }

    public void setResponsaveisIdsRegistrados(String responsaveisIds) {
        this.responsaveisIds = responsaveisIds;
    }

    private void atualizarResponsaveisIds() {
        String idsConcatenados = responsaveis.stream()
                .map(Contato::getIdContato)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        if (idsConcatenados.isBlank()) {
            this.responsaveisIds = null;
        } else {
            this.responsaveisIds = idsConcatenados;
        }
    }

    @JsonProperty("responsaveisIds")
    public List<Long> getResponsaveisIds() {
        return responsaveis.stream()
                .map(Contato::getIdContato)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<TarefaComentario> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<TarefaComentario> comentarios) {
        this.comentarios = comentarios;
    }

    public Set<TarefaComentarioNotificacao> getNotificacoesComentario() {
        return notificacoesComentario;
    }

    public void setNotificacoesComentario(Set<TarefaComentarioNotificacao> notificacoesComentario) {
        this.notificacoesComentario = notificacoesComentario;
    }
}