package com.example.unihub.data.model

data class AvaliacoesConfig(
    val periodicidade: Map<Prioridade, Antecedencia> = defaultPeriodicidade()
)

fun defaultPeriodicidade(): Map<Prioridade, Antecedencia> =
    Prioridade.values().associateWith { Antecedencia.padrao }

data class NotificacoesConfig(
    // Seção Disciplinas
    val notificacaoDePresenca: Boolean = true,
    val avaliacoesAtivas: Boolean = true,
    val avaliacoesConfig: AvaliacoesConfig = AvaliacoesConfig(),
    val compartilhamentoDisciplina: Boolean = true,

    // Seção Quadros/Tarefas
    val incluirEmQuadro: Boolean = true,
    val prazoTarefa: Boolean = true,
    val comentarioTarefa: Boolean = true,

    // Seção Contatos/Grupos
    val conviteContato: Boolean = true,
    val inclusoEmGrupo: Boolean = true
)

fun NotificacoesConfig.normalized(): NotificacoesConfig {
    val periodicidadeNormalizada = avaliacoesConfig.periodicidade.toMutableMap()
    Prioridade.values().forEach { prioridade ->
        periodicidadeNormalizada.putIfAbsent(prioridade, Antecedencia.padrao)
    }

    return copy(
        avaliacoesConfig = avaliacoesConfig.copy(
            periodicidade = periodicidadeNormalizada.toMap()
        )
    )
}

fun NotificacoesConfig.deepCopy(): NotificacoesConfig = copy(
    avaliacoesConfig = avaliacoesConfig.copy(
        periodicidade = avaliacoesConfig.periodicidade.toMap()
    )
)