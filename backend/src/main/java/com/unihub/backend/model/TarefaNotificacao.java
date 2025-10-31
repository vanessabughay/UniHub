package com.unihub.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tarefas_comentarios_notificacoes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tarefa_id", "usuario_id"}))
public class TarefaNotificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_id", nullable = false)
    private TarefaPlanejamento tarefa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TarefaPlanejamento getTarefa() {
        return tarefa;
    }

    public void setTarefa(TarefaPlanejamento tarefa) {
        this.tarefa = tarefa;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}