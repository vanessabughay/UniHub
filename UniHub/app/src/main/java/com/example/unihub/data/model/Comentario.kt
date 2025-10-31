package com.example.unihub.data.model

import com.google.gson.annotations.SerializedName

data class Comentario(
    val id: String,
    val conteudo: String,
    val autorId: Long,
    val autorNome: String,
    @SerializedName("autor")
    val isAutor: Boolean,
    val dataCriacao: Long?,
    val dataAtualizacao: Long?
)

data class ComentariosResponse(
    val comentarios: List<Comentario>,
    val receberNotificacoes: Boolean
)

data class TarefaPreferenciaResponse(
    val receberNotificacoes: Boolean
)