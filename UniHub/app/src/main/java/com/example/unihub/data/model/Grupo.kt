package com.example.unihub.data.model

data class Grupo(
    override var id: Long? = null,
    override var nome: String,
    var membros: List<Contato>,

    ) : Integrante
