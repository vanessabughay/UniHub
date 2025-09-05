package com.example.unihub.data.model

data class Grupo(
    var id: Long? = null,
    var nome: String,
    var membros: List<Contato>,

    )
