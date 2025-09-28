package com.example.unihub.data.api.model

data class AuthResponse(
    val token: String,
    val nomeUsuario: String = "",
    val usuarioId: Long? = null,
)