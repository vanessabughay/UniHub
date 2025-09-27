package com.example.unihub.data.api

import com.example.unihub.data.model.Disciplina
import retrofit2.Response
import retrofit2.http.*

interface DisciplinaApi {
    @GET("disciplinas")
    //suspend fun list(): List<Disciplina>
    suspend fun list(): Response<List<Disciplina>>

    @GET("disciplinas/{id}")
    //suspend fun get(@Path("id") id: Long): Disciplina
    suspend fun get(@Path("id") id: Long): Response<Disciplina>

    @POST("disciplinas")
    //suspend fun add(@Body disciplina: Disciplina): Disciplina
    suspend fun add(@Body disciplina: Disciplina): Response<Disciplina>

    @PUT("disciplinas/{id}")
    //suspend fun update(@Path("id") id: Long, @Body disciplina: Disciplina): Disciplina
    suspend fun update(@Path("id") id: Long, @Body disciplina: Disciplina): Response<Disciplina>

    @DELETE("disciplinas/{id}")
    //suspend fun delete(@Path("id") id: Long)
    suspend fun delete(@Path("id") id: Long): Response<Void>
}
