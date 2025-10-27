package com.example.unihub.data.model

data class AvaliacoesConfig(
    val periodicidade: Map<Prioridade, Antecedencia> = defaultPeriodicidade()
)

fun defaultPeriodicidade(): Map<Prioridade, Antecedencia> =
    Prioridade.values().associateWith { Antecedencia.padrao }

data class NotificacoesConfig(
    val notificacaoDePresenca: Boolean = true,
    val avaliacoesAtivas: Boolean = true,
    val avaliacoesConfig: AvaliacoesConfig = AvaliacoesConfig()
)