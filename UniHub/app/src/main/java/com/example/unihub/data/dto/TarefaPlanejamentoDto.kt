package com.example.unihub.data.dto

import java.time.LocalDate

data class TarefaPlanejamentoRequestDto(
    val titulo: String,
    val descricao: String?,
    val dataPrazo: LocalDate,
    val responsavelIds: List<Long> = emptyList()
)