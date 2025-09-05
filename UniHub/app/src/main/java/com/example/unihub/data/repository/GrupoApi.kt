package com.example.unihub.data.repository

import com.example.unihub.data.model.Grupo
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface GrupoApi {
    @GET("grupo")
    suspend fun list(): List<Grupo>

    @GET("grupo/{id}")
    suspend fun get(@Path("id") id: Long): Grupo

    @POST("grupo")
    suspend fun add(@Body grupo: Grupo): Grupo

    @PUT("grupo/{id}")
    suspend fun update(@Path("id") id: Long, @Body grupo: Grupo): Grupo

    @DELETE("grupo/{id}")
    suspend fun delete(@Path("id") id: Long)
}