package com.example.unihub.data.model


data class Contato(
    override var id: Long? = null,
    override var nome: String? = null,
    var email: String? = null,
    var pendente: Boolean = true,
    var idContato: Long? = null,
    var ownerId: Long? = null,
    var dataSolicitacao: String? = null,
    var dataConfirmacao: String? = null
) : Integrante
