package com.example.unihub.data.dto

data class AtualizarTarefaPlanejamentoRequestDto(
    val titulo: String?,
    val descricao: String?,
    val status: String?,
    val prazo: String?,
    val responsavelIds: List<Long>
)