package com.example.unihub.data.model

import java.time.LocalDate
import java.time.LocalDateTime


data class Avaliacao(
    var id: Long? = null,
    var descricao: String? = null,
    var disciplina: Disciplina? = null,
    var tipoAvaliacao: String? = null,
    var modalidade: Modalidade? = null,
    var dataEntrega: String? = null,
    var nota: Double? = null,
    var peso: Double? = null,
    var integrantes: List<Contato>,
    var prioridade: Prioridade,
    var estado: EstadoAvaliacao,
    var dificuldade: Int? = null,
    var receberNotificacoes: Boolean = true

    )

enum class EstadoAvaliacao(val displayName: String) {
    A_REALIZAR("A REALIZAR"),
    EM_ANDAMENTO("EM ANDAMENTO"),
    CONCLUIDA("CONCLUÍDA")
}

enum class Prioridade(val displayName: String) {
    MUITO_BAIXA("MUITO BAIXA"),
    BAIXA("BAIXA"),
    MEDIA("MEDIA"),
    ALTA("ALTA"),
    MUITO_ALTA("MUITO ALTA")
}

enum class Modalidade(val displayName: String) {
    INDIVIDUAL("INDIVIDUAL"),
    EM_GRUPO("EM GRUPO")
}