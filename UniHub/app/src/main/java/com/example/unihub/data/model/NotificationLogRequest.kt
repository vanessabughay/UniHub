package com.example.unihub.data.model

data class NotificationLogRequest(
    val titulo: String?,
    val mensagem: String,
    val tipo: String?,
    val categoria: String?,
    val referenciaId: Long?,
    val interacaoPendente: Boolean?,
    val metadata: Map<String, Any?>?,
    val timestamp: Long
)