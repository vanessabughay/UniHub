package com.example.unihub.data.dto

data class ComentarioRequestDto(
    val conteudo: String
)

data class TarefaNotificacaoRequestDto(
    val receberNotificacoes: Boolean
)