package com.example.unihub.data.dto

data class TarefaPlanejamentoRequestDto(
    val titulo: String,
    val descricao: String?,
    val dataPrazo: String?,
    val responsavelIds: List<Long> = emptyList()
)