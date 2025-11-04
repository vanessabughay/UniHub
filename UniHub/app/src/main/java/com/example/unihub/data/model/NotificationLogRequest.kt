package com.example.unihub.data.model

data class NotificationLogRequest(
    val titulo: String?,
    val mensagem: String,
    val tipo: String?,
    val referenciaId: Long?,
    val timestamp: Long
)