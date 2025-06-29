package com.example.unihub.data.repository

import com.example.unihub.data.model.Disciplina
import retrofit2.http.*

interface DisciplinaApi {
    @GET("disciplinas")
    suspend fun list(): List<Disciplina>

    @GET("disciplinas/{id}")
    suspend fun get(@Path("id") id: Long): Disciplina

    @POST("disciplinas")
    suspend fun add(@Body disciplina: Disciplina): Disciplina

    @PUT("disciplinas/{id}")
    suspend fun update(@Path("id") id: Long, @Body disciplina: Disciplina): Disciplina

    @DELETE("disciplinas/{id}")
    suspend fun delete(@Path("id") id: Long)
}
