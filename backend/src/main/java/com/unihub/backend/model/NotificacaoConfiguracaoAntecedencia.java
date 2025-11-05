package com.unihub.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.unihub.backend.model.enums.Antecedencia;
import com.unihub.backend.model.enums.Prioridade;
import jakarta.persistence.*;

@Entity
@Table(name = "notificacao_config_antecedencias",
       uniqueConstraints = @UniqueConstraint(columnNames = {"configuracao_id", "prioridade"}))
public class NotificacaoConfiguracaoAntecedencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuracao_id", nullable = false)
    @JsonIgnore
    private NotificacaoConfiguracao configuracao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prioridade prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Antecedencia antecedencia;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NotificacaoConfiguracao getConfiguracao() {
        return configuracao;
    }

    public void setConfiguracao(NotificacaoConfiguracao configuracao) {
        this.configuracao = configuracao;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    public Antecedencia getAntecedencia() {
        return antecedencia;
    }

    public void setAntecedencia(Antecedencia antecedencia) {
        this.antecedencia = antecedencia;
    }
}