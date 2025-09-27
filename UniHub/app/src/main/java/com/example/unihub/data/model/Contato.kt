package com.example.unihub.data.model


data class Contato(
    var id: Long? = null,
    var nome: String? = null,
    var email: String? = null,
    var pendente: Boolean = true
)