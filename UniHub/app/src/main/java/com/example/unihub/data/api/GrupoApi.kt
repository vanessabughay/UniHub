package com.example.unihub.data.api

import com.example.unihub.data.model.Grupo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface GrupoApi {
    @GET("api/grupos") // Corresponde a @GetMapping em GrupoController
    suspend fun list(
        @Header("Authorization") authHeader: String
    ): Response<List<Grupo>>

    @GET("api/grupos/{id}") // Corresponde a @GetMapping("/{id}")
    suspend fun get(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long
    ): Response<Grupo>

    @POST("api/grupos") // Corresponde a @PostMapping
    suspend fun add(
        @Header("Authorization") authHeader: String,
        @Body grupo: Grupo
    ): Response<Grupo>

    @PUT("api/grupos/{id}") // Corresponde a @PutMapping("/{id}")
    suspend fun update(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long,
        @Body grupo: Grupo
    ): Response<Grupo>

    @DELETE("api/grupos/{id}") // Corresponde a @DeleteMapping("/{id}")
    suspend fun delete(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long
    ): Response<Void>

    @DELETE("api/grupos/{id}/sair")
    suspend fun leave(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long
    ): Response<Void>

}