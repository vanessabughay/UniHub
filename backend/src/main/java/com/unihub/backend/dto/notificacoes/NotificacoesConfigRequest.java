package com.unihub.backend.dto.notificacoes;

public class NotificacoesConfigRequest {

    private Boolean notificacaoDePresenca;
    private Boolean avaliacoesAtivas;
    private AvaliacoesConfigDto avaliacoesConfig;

    // NOVOS CAMPOS
    private Boolean compartilhamentoDisciplina;
    private Boolean incluirEmQuadro;
    private Boolean prazoTarefa;
    private Boolean comentarioTarefa;
    private Boolean conviteContato;
    private Boolean inclusoEmGrupo;

    // --- Getters e Setters ---

    public Boolean getNotificacaoDePresenca() {
        return notificacaoDePresenca;
    }

    public void setNotificacaoDePresenca(Boolean notificacaoDePresenca) {
        this.notificacaoDePresenca = notificacaoDePresenca;
    }

    public Boolean getAvaliacoesAtivas() {
        return avaliacoesAtivas;
    }

    public void setAvaliacoesAtivas(Boolean avaliacoesAtivas) {
        this.avaliacoesAtivas = avaliacoesAtivas;
    }

    public AvaliacoesConfigDto getAvaliacoesConfig() {
        return avaliacoesConfig;
    }

    public void setAvaliacoesConfig(AvaliacoesConfigDto avaliacoesConfig) {
        this.avaliacoesConfig = avaliacoesConfig;
    }

    public Boolean getCompartilhamentoDisciplina() {
        return compartilhamentoDisciplina;
    }

    public void setCompartilhamentoDisciplina(Boolean compartilhamentoDisciplina) {
        this.compartilhamentoDisciplina = compartilhamentoDisciplina;
    }

    public Boolean getIncluirEmQuadro() {
        return incluirEmQuadro;
    }

    public void setIncluirEmQuadro(Boolean incluirEmQuadro) {
        this.incluirEmQuadro = incluirEmQuadro;
    }

    public Boolean getPrazoTarefa() {
        return prazoTarefa;
    }

    public void setPrazoTarefa(Boolean prazoTarefa) {
        this.prazoTarefa = prazoTarefa;
    }

    public Boolean getComentarioTarefa() {
        return comentarioTarefa;
    }

    public void setComentarioTarefa(Boolean comentarioTarefa) {
        this.comentarioTarefa = comentarioTarefa;
    }

    public Boolean getConviteContato() {
        return conviteContato;
    }

    public void setConviteContato(Boolean conviteContato) {
        this.conviteContato = conviteContato;
    }

    public Boolean getInclusoEmGrupo() {
        return inclusoEmGrupo;
    }

    public void setInclusoEmGrupo(Boolean inclusoEmGrupo) {
        this.inclusoEmGrupo = inclusoEmGrupo;
    }
}