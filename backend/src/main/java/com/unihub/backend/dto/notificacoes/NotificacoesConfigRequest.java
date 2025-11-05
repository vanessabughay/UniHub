package com.unihub.backend.dto.notificacoes;

public class NotificacoesConfigRequest {

    private Boolean notificacaoDePresenca;
    private Boolean avaliacoesAtivas;
    private AvaliacoesConfigDto avaliacoesConfig;

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
}