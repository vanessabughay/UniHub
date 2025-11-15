package com.unihub.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "notificacao_configuracoes")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class NotificacaoConfiguracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    // --- Seção Disciplinas (Existente) ---
    @Column(name = "notificacao_de_presenca", nullable = false)
    private boolean notificacaoDePresenca = true;

    @Column(name = "avaliacoes_ativas", nullable = false)
    private boolean avaliacoesAtivas = true;

    @Column(name = "compartilhamento_disciplina", nullable = false)
    private boolean compartilhamentoDisciplina = true;

    // --- Seção Quadros/Tarefas (NOVOS CAMPOS) ---
    @Column(name = "incluir_em_quadro", nullable = false)
    private boolean incluirEmQuadro = true;

    @Column(name = "prazo_tarefa", nullable = false)
    private boolean prazoTarefa = true;

    @Column(name = "comentario_tarefa", nullable = false)
    private boolean comentarioTarefa = true;

    // --- Seção Contatos/Grupos (NOVOS CAMPOS) ---
    @Column(name = "convite_contato", nullable = false)
    private boolean conviteContato = true;

    @Column(name = "incluso_em_grupo", nullable = false)
    private boolean inclusoEmGrupo = true;

    @OneToMany(mappedBy = "configuracao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<NotificacaoConfiguracaoAntecedencia> antecedencias = new LinkedHashSet<>();

    // --- Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public boolean isNotificacaoDePresenca() {
        return notificacaoDePresenca;
    }

    public void setNotificacaoDePresenca(boolean notificacaoDePresenca) {
        this.notificacaoDePresenca = notificacaoDePresenca;
    }

    public boolean isAvaliacoesAtivas() {
        return avaliacoesAtivas;
    }

    public void setAvaliacoesAtivas(boolean avaliacoesAtivas) {
        this.avaliacoesAtivas = avaliacoesAtivas;
    }

    public boolean isCompartilhamentoDisciplina() {
        return compartilhamentoDisciplina;
    }

    public void setCompartilhamentoDisciplina(boolean compartilhamentoDisciplina) {
        this.compartilhamentoDisciplina = compartilhamentoDisciplina;
    }

    public boolean isIncluirEmQuadro() {
        return incluirEmQuadro;
    }

    public void setIncluirEmQuadro(boolean incluirEmQuadro) {
        this.incluirEmQuadro = incluirEmQuadro;
    }

    public boolean isPrazoTarefa() {
        return prazoTarefa;
    }

    public void setPrazoTarefa(boolean prazoTarefa) {
        this.prazoTarefa = prazoTarefa;
    }

    public boolean isComentarioTarefa() {
        return comentarioTarefa;
    }

    public void setComentarioTarefa(boolean comentarioTarefa) {
        this.comentarioTarefa = comentarioTarefa;
    }

    public boolean isConviteContato() {
        return conviteContato;
    }

    public void setConviteContato(boolean conviteContato) {
        this.conviteContato = conviteContato;
    }

    public boolean isInclusoEmGrupo() {
        return inclusoEmGrupo;
    }

    public void setInclusoEmGrupo(boolean inclusoEmGrupo) {
        this.inclusoEmGrupo = inclusoEmGrupo;
    }

    public Set<NotificacaoConfiguracaoAntecedencia> getAntecedencias() {
        return antecedencias;
    }

    public void setAntecedencias(Set<NotificacaoConfiguracaoAntecedencia> antecedencias) {
        this.antecedencias.clear();
        if (antecedencias == null) {
            return;
        }
        antecedencias.forEach(this::addAntecedencia);
    }

    public void addAntecedencia(NotificacaoConfiguracaoAntecedencia antecedencia) {
        antecedencias.add(antecedencia);
        antecedencia.setConfiguracao(this);
    }

    public void removeAntecedencia(NotificacaoConfiguracaoAntecedencia antecedencia) {
        antecedencias.remove(antecedencia);
        antecedencia.setConfiguracao(null);
    }
}