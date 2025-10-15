package com.unihub.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.unihub.backend.model.enums.EstadoPlanejamento;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "colunas_planejamento")
public class ColunaPlanejamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPlanejamento estado = EstadoPlanejamento.EM_ANDAMENTO;

    private Integer ordem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quadro_id", nullable = false)
    @JsonBackReference("quadro-colunas")
    private QuadroPlanejamento quadro;

    @OneToMany(mappedBy = "coluna", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("coluna-tarefas")
    private List<TarefaPlanejamento> tarefas = new ArrayList<>();

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

    @JsonIgnore
    public EstadoPlanejamento getEstado() {
        return estado;
    }

    public void setEstado(EstadoPlanejamento estado) {
        this.estado = estado;
    }

    @JsonProperty("status")
    public String getStatusJson() {
        if (estado == null) {
            return "INICIADA";
        }
        return estado == EstadoPlanejamento.CONCLUIDO ? "CONCLUIDA" : "INICIADA";
    }

    @JsonSetter("status")
    public void setStatusJson(String status) {
        if (status == null) {
            this.estado = EstadoPlanejamento.EM_ANDAMENTO;
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

    public QuadroPlanejamento getQuadro() {
        return quadro;
    }

    public void setQuadro(QuadroPlanejamento quadro) {
        this.quadro = quadro;
    }

    public List<TarefaPlanejamento> getTarefas() {
        return tarefas;
    }

    public void setTarefas(List<TarefaPlanejamento> tarefas) {
        this.tarefas = tarefas;
    }
}