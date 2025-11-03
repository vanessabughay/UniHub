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

    @Column(name = "notificacao_de_presenca", nullable = false)
    private boolean notificacaoDePresenca = true;

    @Column(name = "avaliacoes_ativas", nullable = false)
    private boolean avaliacoesAtivas = true;

    @OneToMany(mappedBy = "configuracao", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NotificacaoConfiguracaoAntecedencia> antecedencias = new LinkedHashSet<>();

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

    public Set<NotificacaoConfiguracaoAntecedencia> getAntecedencias() {
        return antecedencias;
    }

    public void setAntecedencias(Set<NotificacaoConfiguracaoAntecedencia> antecedencias) {
        this.antecedencias = antecedencias;
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