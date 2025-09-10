package com.example.unihub.data.model

data class Anotacao(
    val id: Long,
    val titulo: String,
    val conteudo: String,
    val expandida: Boolean = false,
    val rascunhoTitulo: String = titulo,
    val rascunhoConteudo: String = conteudo
)