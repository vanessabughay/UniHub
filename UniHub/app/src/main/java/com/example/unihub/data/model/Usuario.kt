package com.example.unihub.data.model

data class UsuarioResumo(
    val id: Long,
    val nome: String,
    val email: String
)

data class CompartilharDisciplinaRequest(
    val disciplinaId: Long,
    val destinatarioId: Long,
    val mensagem: String? = null
)

data class ConviteCompartilhamentoResponse(
    val id: Long,
    val disciplinaId: Long?,
    val remetenteId: Long?,
    val destinatarioId: Long?,
    val status: String,
    val mensagem: String?
)

data class NotificacaoResponse(
    val id: Long,
    val mensagem: String,
    val lida: Boolean,
    val tipo: String?,
    val conviteId: Long?,
    val criadaEm: String?
)