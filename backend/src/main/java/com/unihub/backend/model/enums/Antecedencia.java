package com.unihub.backend.model.enums;

import java.time.Duration;

public enum Antecedencia {
    NA_HORA(Duration.ZERO),
    UMA_HORA(Duration.ofHours(1)),
    DUAS_HORAS(Duration.ofHours(2)),
    TRES_HORAS(Duration.ofHours(3)),
    SEIS_HORAS(Duration.ofHours(6)),
    DOZE_HORAS(Duration.ofHours(12)),
    UM_DIA(Duration.ofHours(24)),
    DOIS_DIAS(Duration.ofDays(2)),
    TRES_DIAS(Duration.ofDays(3)),
    UMA_SEMANA(Duration.ofDays(7)),
    DUAS_SEMANAS(Duration.ofDays(14));

    private final Duration duracao;

    Antecedencia(Duration duracao) {
        this.duracao = duracao;
    }

    public Duration getDuracao() {
        return duracao;
    }

    public static Antecedencia padrao() {
        return NA_HORA;
    }
}