package com.unihub.backend.dto.notificacoes;

import com.unihub.backend.model.enums.Antecedencia;
import com.unihub.backend.model.enums.Prioridade;

import java.util.EnumMap;
import java.util.Map;

public class AvaliacoesConfigDto {

    private Map<Prioridade, Antecedencia> periodicidade = new EnumMap<>(Prioridade.class);

    public Map<Prioridade, Antecedencia> getPeriodicidade() {
        return periodicidade;
    }

    public void setPeriodicidade(Map<Prioridade, Antecedencia> periodicidade) {
        if (periodicidade == null) {
            this.periodicidade = new EnumMap<>(Prioridade.class);
        } else {
            this.periodicidade = new EnumMap<>(periodicidade);
        }
    }
}