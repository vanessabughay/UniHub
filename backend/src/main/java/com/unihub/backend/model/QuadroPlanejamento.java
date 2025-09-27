package com.unihub.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.unihub.backend.model.enums.QuadroStatus;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "quadros_planejamento")
public class QuadroPlanejamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 2000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuadroStatus status = QuadroStatus.ATIVO;

    @Column(name = "data_criacao", nullable = false)
    private Instant dataCriacao = Instant.now();

    @Column(name = "data_prazo")
    private Instant dataPrazo;

    @Column(name = "disciplina_nome")
    private String disciplina;

    @ElementCollection
    @CollectionTable(name = "quadros_planejamento_integrantes", joinColumns = @JoinColumn(name = "quadro_id"))
    @Column(name = "integrante")
    private List<String> integrantes = new ArrayList<>();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonBackReference("usuario-quadros")
    private Usuario usuario;

    @ManyToMany
    @JoinTable(name = "quadros_planejamento_membros",
            joinColumns = @JoinColumn(name = "quadro_id"),
            inverseJoinColumns = @JoinColumn(name = "contato_id"))
    private Set<Contato> membros = new HashSet<>();

    @OneToMany(mappedBy = "quadro", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("quadro-colunas")
    private List<ColunaPlanejamento> colunas = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("nome")
    public String getTitulo() {
        return titulo;
    }

    @JsonProperty("nome")
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    
    @JsonProperty("estado")
    public QuadroStatus getStatus() {
        return status;
    }

    @JsonProperty("estado")
    public void setStatus(QuadroStatus status) {
        this.status = status;
    }

    @JsonProperty("dataInicio")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    public Instant getDataCriacao() {
        return dataCriacao;
    }

    @JsonProperty("dataInicio")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    public void setDataCriacao(Instant dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    @JsonProperty("dataFim")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    public Instant getDataPrazo() {
        return dataPrazo;
    }

    @JsonProperty("dataFim")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    public void setDataPrazo(Instant dataPrazo) {
        this.dataPrazo = dataPrazo;
    }

    @JsonProperty("disciplina")
    public String getDisciplina() {
        return disciplina;
    }

    @JsonProperty("disciplina")
    public void setDisciplina(String disciplina) {
        this.disciplina = disciplina;
    }

@JsonProperty("integrantes")
    public List<String> getIntegrantes() {
        return integrantes;
    }

    @JsonProperty("integrantes")
    public void setIntegrantes(List<String> integrantes) {
        this.integrantes = integrantes != null ? new ArrayList<>(integrantes) : new ArrayList<>();
    }

    @JsonIgnore
    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @JsonProperty("donoId")
    public Long getDonoId() {
        return usuario != null ? usuario.getId() : null;
    }
    
    public Set<Contato> getMembros() {
        return membros;
    }

    public void setMembros(Set<Contato> membros) {
        this.membros = membros;
    }

    public List<ColunaPlanejamento> getColunas() {
        return colunas;
    }

    public void setColunas(List<ColunaPlanejamento> colunas) {
        this.colunas = colunas;
    }
}

