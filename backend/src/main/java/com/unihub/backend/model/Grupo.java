package com.unihub.backend.model;

import java.util.ArrayList;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


public class Grupo {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)

private Long id;
private String nomeGrupo;
private ArrayList<Contato> contatoLista;


    // Construtor vazio (necessário para JPA)
    public Grupo() {
    }

    // Construtor com campos (útil para testes ou criação)
    public Grupo(String nome, ArrayList<Contato> contatoLista) {
        this.nomeGrupo = nome;
        this.contatoLista = contatoLista;

    }

    // Getters e Setters (necessários para JPA e para o Spring conseguir serializar/desserializar)

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeGrupo() {
        return nomeGrupo;
    }

    public void setNomeGrupo(String nome) {
        this.nomeGrupo = nome;
    }

    public ArrayList<Contato> getContatoLista() {
        return contatoLista;
    }
    public void setContatoLista(ArrayList<Contato> contatoLista) {
        this.contatoLista = contatoLista;
    }
    public void addContato(Contato contato) {
        this.contatoLista.add(contato);
    }
    public void removeContato(Contato contato) {
        this.contatoLista.remove(contato);
    }
    public void removeContato(int index) {
        this.contatoLista.remove(index);
    }



    @Override
    public String toString() {
        return "Grupo{" +
                "id=" + id +
                "nomeGrupo='" + nomeGrupo + '\'' +
                ", contatoLista=" + contatoLista +
                '}';
    }


}
