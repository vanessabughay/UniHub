package com.unihub.backend.dto.notificacoes;

public class NotificacoesConfigResponse {

    private boolean notificacaoDePresenca;
    private boolean avaliacoesAtivas;
    private AvaliacoesConfigDto avaliacoesConfig;

    // NOVOS CAMPOS
    private boolean compartilhamentoDisciplina;
    private boolean incluirEmQuadro;
    private boolean prazoTarefa;
    private boolean comentarioTarefa;
    private boolean conviteContato;
    private boolean inclusoEmGrupo;

    // --- Getters e Setters ---

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

    public AvaliacoesConfigDto getAvaliacoesConfig() {
        return avaliacoesConfig;
    }

    public void setAvaliacoesConfig(AvaliacoesConfigDto avaliacoesConfig) {
        this.avaliacoesConfig = avaliacoesConfig;
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
}