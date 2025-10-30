package com.example.unihub.data.dto

import java.time.LocalDateTime

data class AtualizarTarefaPlanejamentoRequestDto(
    val titulo: String?,
    val descricao: String?,
    val status: String?,
    val prazo: LocalDateTime?,
    val responsavelIds: List<Long>
)