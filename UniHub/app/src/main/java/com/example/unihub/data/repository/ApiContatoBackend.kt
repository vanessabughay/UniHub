package com.example.unihub.data.repository // Mesmo pacote ou importe Contatobackend

import com.example.unihub.data.api.RetrofitClient
import com.example.unihub.data.model.Contato
import com.example.unihub.data.util.LocalDateAdapter
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate



class ApiContatoBackend : Contatobackend {
    private val api: ContatoApi by lazy {
        RetrofitClient.create(ContatoApi::class.java) // usa o retrofit com interceptor
    }


    override suspend fun getContatoResumoApi(): List<ContatoResumo> {
        return api.list().map { ContatoResumo(it.id!!, it.nome!!, it.email!!) }
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
