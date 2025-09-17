package com.unihub.backend.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;


@Entity
public class Contato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String email;
    private Boolean pendente;

    @Column(nullable = false)
    private Long ownerId;

    // Construtor vazio (necessário para JPA)
    public Contato() {
    }

    // Construtor com campos (útil para testes ou criação)
    public Contato(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

    // Getters e Setters (necessários para JPA e para o Spring conseguir serializar/desserializar)
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public Boolean getPendente() {
        return pendente;
    }
    public void setPendente(Boolean pendente) {
        this.pendente = pendente;
    }
    public Long getOwnerId() { 
        return ownerId; 
    }
    public void setOwnerId(Long ownerId) { 
        this.ownerId = ownerId; 
    }




    @Override
    public String toString() {
        return "Contato{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", pendente=" + pendente +
                '}';
    }

}
