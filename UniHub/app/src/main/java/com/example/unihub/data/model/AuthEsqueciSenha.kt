package com.example.unihub.data.api.model

import com.google.gson.annotations.SerializedName

data class SolicitarRedefinicaoSenhaRequest(
    @SerializedName("email") val email: String
)

data class RedefinirSenhaRequest(
    @SerializedName("token") val token: String,
    @SerializedName("newPassword") val novaSenha: String
)
