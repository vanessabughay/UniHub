package com.unihub.backend.model;

import jakarta.persistence.*; // Certifique-se de usar o pacote correto (jakarta ou javax)
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Para equals e hashCode


@Entity
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // Exemplo: nome não pode ser nulo
    private String nome;

    @Column(nullable = false)
    private Long ownerId;

    @Transient
    private Long adminContatoId; // ID do contato dentro da lista de membros que atua como administrador

    // Se for ManyToMany (um Contato pode estar em vários Grupos e um Grupo pode ter vários Contatos)
    @ManyToMany(fetch = FetchType.LAZY, // Lazy loading é geralmente bom para coleções
            cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "grupo_membros_contato", // Nome da tabela de junção
            joinColumns = @JoinColumn(name = "grupo_id"),
            inverseJoinColumns = @JoinColumn(name = "id_contato", referencedColumnName = "id_contato")
    )
    private List<Contato> membros = new ArrayList<>(); // Nome da coleção: 'membros'

    // Construtor vazio (requerido pelo JPA)
    public Grupo() {
    }

    // Construtor com nome
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

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getAdminContatoId() {
        return adminContatoId;
    }

    public void setAdminContatoId(Long adminContatoId) {
        this.adminContatoId = adminContatoId;
    }

    public List<Contato> getMembros() {
        return membros;
    }

    public void setMembros(List<Contato> membros) {
        if (membros == null) {
            this.membros = new ArrayList<>();
        } else {
            this.membros = membros;
        }
    }
    


    // Métodos utilitários para gerenciar a coleção (boa prática)
    public void addMembro(Contato contato) {
        if (contato != null && !this.membros.contains(contato)) {
            this.membros.add(contato);

        }
    }

    public void removeMembro(Contato contato) {
        if (contato != null) {
            this.membros.remove(contato);

        }
    }

    public void clearMembros() {
        this.membros.clear();
    }

    // Sobrescreva equals e hashCode se você adicionar/remover itens de coleções de entidades gerenciadas
    // e essas coleções forem Sets, ou se você for comparar instâncias de Grupo.
    // Baseado no ID é comum para entidades.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grupo grupo = (Grupo) o;
        return Objects.equals(id, grupo.id); // Apenas o ID é suficiente se for único e não nulo após persistência
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Apenas o ID
    }

    @Override
    public String toString() {
        return "Grupo{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", ownerId=" + ownerId +
                ", adminContatoId=" + adminContatoId +
                ", numeroDeMembros=" + (membros != null ? membros.size() : "0 (lista nula)") +
                '}';
    }
}
