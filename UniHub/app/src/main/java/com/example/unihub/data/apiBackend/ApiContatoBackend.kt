package com.example.unihub.data.apiBackend // Mesmo pacote ou importe Contatobackend

import com.example.unihub.data.api.ContatoApi
import com.example.unihub.data.config.RetrofitClient
import com.example.unihub.data.model.Contato
import com.example.unihub.data.repository.ContatoResumo
import com.example.unihub.data.repository.Contatobackend


class ApiContatoBackend : Contatobackend {
    private val api: ContatoApi by lazy {
        RetrofitClient.create(ContatoApi::class.java) // usa o retrofit com interceptor
    }


    override suspend fun getContatoResumoApi(): List<ContatoResumo> {
        return api.list()
            .mapNotNull { it.toContatoResumoOrNull() }
    }

    override suspend fun getContatoByIdApi(id: String): Contato? {
        val contatoId = id.toLongOrNull() ?: return null

        return api.list().firstOrNull { it.idContato == contatoId }
            ?: api.get(contatoId)
    }

    override suspend fun addContatoApi(contato: Contato) {
        api.add(contato)
    }

    override suspend fun updateContatoApi(id: Long, contato: Contato): Boolean {
        api.update(id, contato)
        return true // Retorno booleano como na interface
    }

    override suspend fun deleteContatoApi(id: Long): Boolean {
        api.delete(id)
        return true // Retorno booleano como na interface
    }

    override suspend fun getConvitesPendentesPorEmail(email: String): List<ContatoResumo> {
        return api.listPendentes(email)
            .mapNotNull { it.toContatoResumoOrNull() }
    }

    override suspend fun acceptInvitation(id: Long) {
        api.acceptInvite(id)
    }

    override suspend fun rejectInvitation(id: Long) {
        api.rejectInvite(id)
    }
}

private fun Contato.toContatoResumoOrNull(): ContatoResumo? {
    val contatoId = idContato ?: id ?: return null
    val safeEmail = email?.takeIf { it.isNotBlank() } ?: ""
    val safeName = nome?.takeIf { it.isNotBlank() } ?: safeEmail.ifBlank { "Contato sem nome" }

    return ContatoResumo(
        id = contatoId,
        nome = safeName,
        email = safeEmail,
        pendente = pendente,
        ownerId = ownerId,
        registroId = id
    )
}