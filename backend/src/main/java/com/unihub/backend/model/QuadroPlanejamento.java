package com.unihub.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.unihub.backend.model.enums.QuadroStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quadros_planejamento")
public class QuadroPlanejamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuadroStatus status = QuadroStatus.ATIVO;

    @Column(name = "data_prazo")
    private LocalDateTime dataPrazo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonBackReference("usuario-quadros")
    private Usuario usuario;

    @OneToMany(mappedBy = "quadro", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    @JsonManagedReference("quadro-colunas")
    private List<ColunaPlanejamento> colunas = new ArrayList<>();

  
    // pra add disciplina contato e grupo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disciplina_id")
    private Disciplina disciplina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contato_id", referencedColumnName = "id_contato")
    private Contato contato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    
    // --- GETTERS E SETTERS

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
        
    @JsonProperty("estado")
    public QuadroStatus getStatus() {
        return status;
    }

    @JsonProperty("estado")
    public void setStatus(QuadroStatus status) {
        this.status = status;
    }

    

    @JsonProperty("dataFim")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getDataPrazo() {
        return dataPrazo;
    }

    @JsonProperty("dataFim")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public void setDataPrazo(LocalDateTime dataPrazo) {
        this.dataPrazo = dataPrazo;
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
    
    public List<ColunaPlanejamento> getColunas() {
        return colunas;
    }

    public void setColunas(List<ColunaPlanejamento> colunas) {
        this.colunas = colunas;
    }


    @JsonIgnore 
    public Disciplina getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
    }

    @JsonIgnore 
    public Contato getContato() {
        return contato;
    }

    public void setContato(Contato contato) {
        this.contato = contato;
    }

    @JsonIgnore 
    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    // O app Android espera receber os campos 'disciplinaId', 'contatoId', 'grupoId'

    @JsonProperty("disciplinaId")
    public Long getDisciplinaId() {
        return (disciplina != null) ? disciplina.getId() : null;
    }

    @JsonProperty("contatoId")
    public Long getContatoId() {
        if (contato == null) {
            return null;
        }
        return contato.getIdContato();
    }

    @JsonProperty("grupoId")
    public Long getGrupoId() {
        return (grupo != null) ? grupo.getId() : null;
    }
}