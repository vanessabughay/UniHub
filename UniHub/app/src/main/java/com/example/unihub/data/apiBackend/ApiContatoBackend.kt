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
        return api.list().map {
            ContatoResumo(
                id = it.id!!,
                nome = it.nome!!,
                email = it.email!!,
                pendente = it.pendente
            )
        }
    }

    override suspend fun getContatoByIdApi(id: String): Contato? {
        return api.get(id.toLong())
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
}
