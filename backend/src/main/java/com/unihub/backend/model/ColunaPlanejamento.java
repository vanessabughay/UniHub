package com.unihub.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    private String descricao;

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

    public EstadoPlanejamento getEstado() {
        return estado;
    }

    public void setEstado(EstadoPlanejamento estado) {
        this.estado = estado;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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