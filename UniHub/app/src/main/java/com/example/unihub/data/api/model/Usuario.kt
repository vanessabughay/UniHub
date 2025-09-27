package com.example.unihub.data.api.model

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)
data class UpdateUsuarioRequest(
    val name: String,
    val email: String,
    val password: String? = null
)