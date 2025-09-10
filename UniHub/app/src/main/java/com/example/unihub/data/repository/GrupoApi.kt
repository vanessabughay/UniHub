package com.example.unihub.data.repository

import com.example.unihub.data.model.Grupo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface GrupoApi {
    @GET("api/grupos") // Corresponde a @GetMapping em GrupoController
    suspend fun list(): Response<List<Grupo>>

    @GET("api/grupos/{id}") // Corresponde a @GetMapping("/{id}")
    suspend fun get(@Path("id") id: Long): Response<Grupo>

    @POST("api/grupos") // Corresponde a @PostMapping
    suspend fun add(@Body grupo: Grupo): Response<Grupo>

    @PUT("api/grupos/{id}") // Corresponde a @PutMapping("/{id}")
    suspend fun update(@Path("id") id: Long, @Body grupo: Grupo): Response<Grupo>

    @DELETE("api/grupos/{id}") // Corresponde a @DeleteMapping("/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Void>

    /*
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

     */
}