package com.example.unihub.data.model

data class UsuarioResumo(
    val id: Long,
    val nome: String,
    val email: String
)

data class CompartilharDisciplinaRequest(
    val disciplinaId: Long,
    val destinatarioId: Long
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
    val titulo: String?,
    val mensagem: String,
    val lida: Boolean,
    val tipo: String?,
    val categoria: String?,
    val conviteId: Long?,
    val referenciaId: Long?,
    val interacaoPendente: Boolean,
    val metadataJson: String?,
    val criadaEm: String?,
    val atualizadaEm: String?,
)