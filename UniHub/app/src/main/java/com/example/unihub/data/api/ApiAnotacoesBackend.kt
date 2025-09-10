package com.example.unihub.data.repository

import com.example.unihub.data.api.AnotacoesApi
import com.example.unihub.data.dto.AnotacoesRequest
import com.example.unihub.data.model.Anotacao
import com.example.unihub.data.util.LocalDateAdapter
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate

class ApiAnotacoesBackend {

    private val api: AnotacoesApi by lazy {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .create()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AnotacoesApi::class.java)
    }

    suspend fun listAnotacoesApi(disciplinaId: Long): List<Anotacao> {
        val response = api.listar(disciplinaId)
        return response.content.map { dto ->
            Anotacao(
                id = dto.id,
                titulo = dto.titulo,
                conteudo = dto.conteudo
            )
        }
    }

    suspend fun getAnotacaoByIdApi(disciplinaId: Long, anotacaoId: Long): Anotacao? {
        return api.obter(disciplinaId, anotacaoId)?.let { dto ->
            Anotacao(
                id = dto.id,
                titulo = dto.titulo,
                conteudo = dto.conteudo
            )
        }
    }

    suspend fun addAnotacaoApi(disciplinaId: Long, request: AnotacoesRequest): Anotacao {
        val dto = api.criar(disciplinaId, request)
        return Anotacao(
            id = dto.id,
            titulo = dto.titulo,
            conteudo = dto.conteudo
        )
    }

    suspend fun updateAnotacaoApi(disciplinaId: Long, anotacaoId: Long, request: AnotacoesRequest): Anotacao {
        val dto = api.atualizar(disciplinaId, anotacaoId, request)
        return Anotacao(
            id = dto.id,
            titulo = dto.titulo,
            conteudo = dto.conteudo
        )
    }

    suspend fun deleteAnotacaoApi(disciplinaId: Long, anotacaoId: Long) {
        api.excluir(disciplinaId, anotacaoId)
    }
}