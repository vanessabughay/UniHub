package com.unihub.backend.model;

import jakarta.persistence.*;

@Entity
public class Contato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "proprietario_id")
    private Usuario proprietario;

    @ManyToOne
    @JoinColumn(name = "contato_id")
    private Usuario contato;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getProprietario() {
        return proprietario;
    }

    public void setProprietario(Usuario proprietario) {
        this.proprietario = proprietario;
    }

    public Usuario getContato() {
        return contato;
    }

    public void setContato(Usuario contato) {
        this.contato = contato;
    }
}
