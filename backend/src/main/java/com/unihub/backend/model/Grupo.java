package com.unihub.backend.model;

import java.util.ArrayList;
import java.util.List; // Usar a interface List é uma boa prática

import jakarta.persistence.CascadeType; // Para operações em cascata
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType; // Para definir como carregar a coleção
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany; // Anotação de relacionamento
// import jakarta.persistence.JoinColumn; // Se você quiser controlar a coluna de junção

@Entity
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    // Mapeamento OneToMany
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    // @JoinColumn(name = "grupo_id") // Opcional: define a coluna de chave estrangeira na tabela Contato
    // Se não usar @JoinColumn, o Hibernate pode criar uma tabela de junção separada para OneToMany unidirecional
    // ou esperar um @ManyToOne no lado de Contato que defina o mapeamento.
    private List<Contato> contatoLista = new ArrayList<>(); // Boa prática: inicializar a coleção e usar Interface

    // Construtor vazio
    public Grupo() {
    }

    // Construtor com nome (contatoLista é inicializada)
    public Grupo(String nome) {
        this.nome = nome;
    }

    // Getters e Setters
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

    public List<Contato> getContatoLista() {
        return contatoLista;
    }

    public void setContatoLista(List<Contato> contatoLista) {
        this.contatoLista = contatoLista;
    }

    // Métodos utilitários para gerenciar a coleção bidirecionalmente (se aplicável)
    public void addContato(Contato contato) {
        this.contatoLista.add(contato);
        // Se o relacionamento for bidirecional, você também precisaria definir o grupo no contato:
        // contato.setGrupo(this);
    }

    public void removeContato(Contato contato) {
        this.contatoLista.remove(contato);
        // Se o relacionamento for bidirecional:
        // contato.setGrupo(null);
    }
    // O método removeContato(int index) pode ser problemático com JPA se não for gerenciado com cuidado.

    @Override
    public String toString() {
        return "Grupo{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                // Evitar imprimir coleções lazy carregadas diretamente no toString para evitar problemas de sessão
                // ", contatoLista=" + (contatoLista != null ? contatoLista.size() : "null") + // Exemplo
                '}';
    }
}

