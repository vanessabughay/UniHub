package com.example.unihub.data.model

data class Comentario(
    val id: String,
    val conteudo: String,
    val autorId: Long,
    val autorNome: String,
    val isAutor: Boolean,
    val dataCriacao: Long?,
    val dataAtualizacao: Long?
)

data class ComentariosResponse(
    val comentarios: List<Comentario>,
    val receberNotificacoes: Boolean
)

data class ComentarioPreferenciaResponse(
    val receberNotificacoes: Boolean
)