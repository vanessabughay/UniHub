package com.example.unihub.data.model

data class AvaliacoesConfig(
    val antecedencia: Map<Prioridade, Antecedencia> = defaultAntecedencias()
)

fun defaultAntecedencias(): Map<Prioridade, Antecedencia> =
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
    val antecedenciaNormalizada = avaliacoesConfig.antecedencia.toMutableMap()
    Prioridade.values().forEach { prioridade ->
        antecedenciaNormalizada.putIfAbsent(prioridade, Antecedencia.padrao)
    }

    return copy(
        avaliacoesConfig = avaliacoesConfig.copy(
            antecedencia = antecedenciaNormalizada.toMap()
        )
    )
}

fun NotificacoesConfig.deepCopy(): NotificacoesConfig = copy(
    avaliacoesConfig = avaliacoesConfig.copy(
        antecedencia = avaliacoesConfig.antecedencia.toMap()
    )
)