package com.example.unihub.data.apiBackend

import com.example.unihub.data.api.DisciplinaApi
import com.example.unihub.data.config.TokenManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.util.LocalDateAdapter
import com.example.unihub.data.repository.DisciplinaResumo
import com.example.unihub.data.repository._disciplinabackend
import com.google.gson.GsonBuilder
import java.time.LocalDate
import okhttp3.OkHttpClient
import java.io.IOException

class ApiDisciplinaBackend : _disciplinabackend {
    private val api: DisciplinaApi by lazy {
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
            .baseUrl("http://10.0.2.2:8080/")   // emulator loopback
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(DisciplinaApi::class.java)
    }

    /*
    override suspend fun getDisciplinasResumoApi(): List<DisciplinaResumo> =
        api.list().map { DisciplinaResumo(it.id!!,it.codigo, it.nome, it.aulas) }

    override suspend fun getDisciplinaByIdApi(id: String): Disciplina? =
        api.get(id.toLong())

    override suspend fun addDisciplinaApi(disciplina: Disciplina) {
        api.add(disciplina)
    }

    override suspend fun updateDisciplinaApi(id: Long, disciplina: Disciplina): Boolean {
        api.update(id, disciplina)
        return true
    }

    override suspend fun deleteDisciplinaApi(id: Long): Boolean {
        api.delete(id)
        return true
    }
    */
    override suspend fun getDisciplinasResumoApi(): List<DisciplinaResumo> {
        val resp = api.list()
        if (!resp.isSuccessful) {
            throw IOException("Erro ao listar disciplinas: ${resp.code()} ${resp.errorBody()?.string()}")
        }
        val disciplinas = resp.body().orEmpty()
        return disciplinas.map { d ->
            // o id não é nulo (senão lança IllegalArgumentException)
            val id = requireNotNull(d.id) { "Disciplina sem id do backend: $d" }

            DisciplinaResumo(
                id = id,
                nome = d.nome.orEmpty()
            )
        }
    }

    override suspend fun getDisciplinaByIdApi(id: String): Disciplina? {
        val resp = api.get(id.toLong())
        return if (resp.isSuccessful) resp.body() else null
    }

    override suspend fun addDisciplinaApi(disciplina: Disciplina) {
        val resp = api.add(disciplina)
        if (!resp.isSuccessful) {
            throw IOException("Erro ao adicionar disciplina: ${resp.code()} ${resp.errorBody()?.string()}")
        }
    }

    override suspend fun updateDisciplinaApi(id: Long, disciplina: Disciplina): Boolean {
        val resp = api.update(id, disciplina)
        return resp.isSuccessful
    }

    override suspend fun deleteDisciplinaApi(id: Long): Boolean {
        val resp = api.delete(id)
        return resp.isSuccessful
    }


}
