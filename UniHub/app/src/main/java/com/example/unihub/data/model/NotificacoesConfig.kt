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