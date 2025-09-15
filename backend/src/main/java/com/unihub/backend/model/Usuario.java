package com.unihub.backend.model;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.PostConstruct; // para criação automática de  usuários
import org.springframework.beans.factory.annotation.Autowired; // para criação automática de  usuários
import org.springframework.stereotype.Component; // para criação automática de  usuários
import org.springframework.security.crypto.password.PasswordEncoder; // para criação automática de  usuários
import com.unihub.backend.repository.UsuarioRepository; // para criação automática de  usuários

@Component // para criação automática de  usuários
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

    @Transient // para criação automática de  usuários
    @Autowired // para criação automática de  usuários
    private UsuarioRepository usuarioRepository; // para criação automática de  usuários

    @Transient // para criação automática de  usuários
    @Autowired // para criação automática de  usuários
    private PasswordEncoder passwordEncoder; // para criação automática de  usuários

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

    // Criação automática de usuários
    @PostConstruct
    private void initUsuarios() {
        criarUsuarioSeNaoExistir("Vanessa", "vanessa@email.com", "vanessa");
        criarUsuarioSeNaoExistir("Victoria", "victoria@email.com", "victoria");
        criarUsuarioSeNaoExistir("Rafaella", "rafaella@email.com", "rafaella");
        criarUsuarioSeNaoExistir("Paulo", "paulo@email.com", "pauloo");
    }

    private void criarUsuarioSeNaoExistir(String nome, String email, String senha) {
        if (usuarioRepository.findByEmail(email).isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setNomeUsuario(nome);
            usuario.setEmail(email);
            usuario.setSenha(passwordEncoder.encode(senha));
            usuarioRepository.save(usuario);
        }
    }

    //fim da criação automática de usuários
}