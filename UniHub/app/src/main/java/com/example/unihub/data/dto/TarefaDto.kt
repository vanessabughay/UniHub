package com.example.unihub.data.dto

data class TarefaDto(
    val titulo: String,
    val dataPrazo: String?,
    val nomeQuadro: String,
    val receberNotificacoes: Boolean
)