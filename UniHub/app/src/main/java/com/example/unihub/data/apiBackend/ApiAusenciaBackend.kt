package com.example.unihub.data.apiBackend

import com.example.unihub.data.api.AnotacoesApi
import com.example.unihub.data.api.AusenciaApi
import com.example.unihub.data.config.RetrofitClient
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.dto.AnotacoesRequest
import com.example.unihub.data.model.Anotacao
import com.example.unihub.data.model.Ausencia
import com.example.unihub.data.util.LocalDateAdapter
import com.google.gson.GsonBuilder
import com.example.unihub.data.repository._ausenciabackend
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate

class ApiAusenciaBackend : _ausenciabackend {
    private val api: AusenciaApi by lazy {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .create()

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                TokenManager.token?.let { token ->
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .build()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AusenciaApi::class.java)
    }

    override suspend fun listAusenciasApi(): List<Ausencia> = api.list()

    override suspend fun getAusenciaByIdApi(id: Long): Ausencia? = api.get(id)

    override suspend fun addAusenciaApi(ausencia: Ausencia) {
        api.add(ausencia)
    }

    override suspend fun updateAusenciaApi(id: Long, ausencia: Ausencia): Boolean {
        api.update(id, ausencia)
        return true
    }

    override suspend fun deleteAusenciaApi(id: Long): Boolean {
        api.delete(id)
        return true
    }
}

class ApiAnotacoesBackend {

    private val api: AnotacoesApi by lazy {
        RetrofitClient.retrofit.create(AnotacoesApi::class.java)
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