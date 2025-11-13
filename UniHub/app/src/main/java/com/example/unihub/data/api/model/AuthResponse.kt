package com.example.unihub.data.api.model

data class AuthResponse(
    val token: String,
    val nomeUsuario: String = "",
    val email: String? = null,
    val usuarioId: Long? = null,
    val googleCalendarLinked: Boolean = false,
    val hasInstitution: Boolean = false,

)