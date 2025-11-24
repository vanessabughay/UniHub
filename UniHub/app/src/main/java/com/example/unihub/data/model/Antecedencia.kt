package com.example.unihub.data.model

import java.time.Duration

enum class Antecedencia(val duration: Duration, val label: String) {
    NA_HORA(Duration.ZERO, "Na hora"),
    UMA_HORA(Duration.ofHours(1), "1 hora antes"),
    DUAS_HORAS(Duration.ofHours(2), "2 horas antes"),
    TRES_HORAS(Duration.ofHours(3), "3 horas antes"),
    SEIS_HORAS(Duration.ofHours(6), "6 horas antes"),
    DOZE_HORAS(Duration.ofHours(12), "12 horas antes"),
    UM_DIA(Duration.ofHours(24), "24 horas antes"),
    DOIS_DIAS(Duration.ofDays(2), "2 dias antes"),
    TRES_DIAS(Duration.ofDays(3), "3 dias antes"),
    UMA_SEMANA(Duration.ofDays(7), "1 semana antes"),
    DUAS_SEMANAS(Duration.ofDays(14), "2 semanas antes");

    companion object {
        val padrao = NA_HORA
        val todas = listOf(
            NA_HORA,
            UMA_HORA,
            DUAS_HORAS,
            TRES_HORAS,
            SEIS_HORAS,
            DOZE_HORAS,
            UM_DIA,
            DOIS_DIAS,
            TRES_DIAS,
            UMA_SEMANA,
            DUAS_SEMANAS
        )
    }
}