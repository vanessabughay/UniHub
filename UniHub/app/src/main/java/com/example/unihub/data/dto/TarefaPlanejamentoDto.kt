package com.example.unihub.data.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class TarefaPlanejamentoRequestDto(
    val titulo: String,
    val descricao: String?,
    val dataPrazo: LocalDateTime,
    val responsavelIds: List<Long> = emptyList()
)