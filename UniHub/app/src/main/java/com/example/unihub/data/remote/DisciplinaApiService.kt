package com.example.unihub.data.remote

import com.example.unihub.data.model.Disciplina
import retrofit2.http.*

interface DisciplinaApiService {

    @GET("disciplinas")
    suspend fun getAll(): List<Disciplina>

    @GET("disciplinas/{id}")
    suspend fun getById(@Path("id") id: Long): Disciplina

    @POST("disciplinas")
    suspend fun create(@Body disciplina: Disciplina): Disciplina

    @PUT("disciplinas/{id}")
    suspend fun update(@Path("id") id: Long, @Body disciplina: Disciplina): Disciplina

    @DELETE("disciplinas/{id}")
    suspend fun delete(@Path("id") id: Long)

    @GET("disciplinas/pesquisa")
    suspend fun buscarPorNome(@Query("nome") nome: String): List<Disciplina>
}
