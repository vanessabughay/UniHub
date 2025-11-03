package com.unihub.backend.dto.notificacoes;

public class NotificacoesConfigResponse {

    private boolean notificacaoDePresenca;
    private boolean avaliacoesAtivas;
    private AvaliacoesConfigDto avaliacoesConfig;

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
}