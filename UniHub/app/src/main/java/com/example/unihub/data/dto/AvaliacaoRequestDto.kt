package com.example.unihub.data.dto

import com.example.unihub.data.model.EstadoAvaliacao
import com.example.unihub.data.model.Modalidade
import com.example.unihub.data.model.Prioridade

/**
 * DTO leve para POST/PUT — manda só o que o backend precisa.
 * Importante: disciplina e integrantes só com ID.
 */
data class AvaliacaoRequestDto(
    val id: Long? = null,                   // usado no PUT
    val descricao: String?,
    val disciplina: DisciplinaIdDto?,         // <- só id
    val tipoAvaliacao: String?,
    val modalidade: Modalidade,             // mesmo enum do app
    val dataEntrega: String?,               // yyyy-MM-dd
    val nota: Double?,
    val peso: Double?,
    val integrantes: List<ContatoIdDto>,      // <- só ids
    val prioridade: Prioridade,
    val estado: EstadoAvaliacao,
    val dificuldade: Int?,
    val receberNotificacoes: Boolean
)
