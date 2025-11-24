package com.unihub.backend.dto.notificacoes;

import com.unihub.backend.model.enums.Antecedencia;
import com.unihub.backend.model.enums.Prioridade;

import java.util.EnumMap;
import java.util.Map;

public class AvaliacoesConfigDto {

    private Map<Prioridade, Antecedencia> antecedencia = new EnumMap<>(Prioridade.class);

    public Map<Prioridade, Antecedencia> getAntecedencia() {
        return antecedencia;
    }

    public void setAntecedencia(Map<Prioridade, Antecedencia> antecedencia) {
        if (antecedencia == null) {
            this.antecedencia = new EnumMap<>(Prioridade.class);
        } else {
            this.antecedencia = new EnumMap<>(antecedencia);
        }
    }
}