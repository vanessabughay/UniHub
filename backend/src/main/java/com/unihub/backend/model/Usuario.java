package com.unihub.backend.model;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import com.unihub.backend.model.QuadroPlanejamento;



@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeUsuario;

    private String email;

    private String senha;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("usuario-disciplinas")
    private List<Disciplina> disciplinas;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("usuario-instituicoes")
    private List<Instituicao> instituicoes;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("usuario-ausencias")
    private List<Ausencia> ausencias;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("usuario-categorias")
    private List<Categoria> categorias;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("usuario-quadros")
    private List<QuadroPlanejamento> quadrosPlanejamento;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public List<Disciplina> getDisciplinas() { return disciplinas; }
    public void setDisciplinas(List<Disciplina> disciplinas) { this.disciplinas = disciplinas; }

    public List<Instituicao> getInstituicoes() { return instituicoes; }
    public void setInstituicoes(List<Instituicao> instituicoes) { this.instituicoes = instituicoes; }

    public List<Ausencia> getAusencias() { return ausencias; }
    public void setAusencias(List<Ausencia> ausencias) { this.ausencias = ausencias; }

    public List<Categoria> getCategorias() { return categorias; }
    public void setCategorias(List<Categoria> categorias) { this.categorias = categorias; }

    public List<QuadroPlanejamento> getQuadrosPlanejamento() { return quadrosPlanejamento; }
    public void setQuadrosPlanejamento(List<QuadroPlanejamento> quadrosPlanejamento) { this.quadrosPlanejamento = quadrosPlanejamento; }
    
}