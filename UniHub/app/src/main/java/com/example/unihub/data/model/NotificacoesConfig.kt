package com.example.unihub.data.model

import com.google.gson.annotations.SerializedName

data class AvaliacoesConfig(
    @SerializedName(value = "antecedencia", alternate = ["antecedencia_map"])
    val antecedencia: Map<Prioridade, Antecedencia> = defaultAntecedencias()
)

fun defaultAntecedencias(): Map<Prioridade, Antecedencia> =
    Prioridade.values().associateWith { Antecedencia.padrao }

data class NotificacoesConfig(
    // Seção Disciplinas
    @SerializedName(value = "notificacaoDePresenca", alternate = ["notificacao_de_presenca"])
    val notificacaoDePresenca: Boolean = true,
    @SerializedName(value = "avaliacoesAtivas", alternate = ["avaliacoes_ativas"])
    val avaliacoesAtivas: Boolean = true,
    @SerializedName(value = "avaliacoesConfig", alternate = ["avaliacoes_config"])
    val avaliacoesConfig: AvaliacoesConfig = AvaliacoesConfig(),
    @SerializedName(value = "compartilhamentoDisciplina", alternate = ["compartilhamento_disciplina"])
    val compartilhamentoDisciplina: Boolean = true,

    // Seção Quadros/Tarefas
    @SerializedName(value = "incluirEmQuadro", alternate = ["incluir_em_quadro"])
    val incluirEmQuadro: Boolean = true,
    @SerializedName(value = "prazoTarefa", alternate = ["prazo_tarefa"])
    val prazoTarefa: Boolean = true,
    @SerializedName(value = "comentarioTarefa", alternate = ["comentario_tarefa"])
    val comentarioTarefa: Boolean = true,

    // Seção Contatos/Grupos
    @SerializedName(value = "conviteContato", alternate = ["convite_contato"])
    val conviteContato: Boolean = true,
    @SerializedName(value = "inclusoEmGrupo", alternate = ["incluso_em_grupo"])
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