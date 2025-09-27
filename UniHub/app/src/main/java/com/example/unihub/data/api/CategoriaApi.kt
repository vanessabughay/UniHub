package com.example.unihub.data.api

import com.example.unihub.data.model.Categoria
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CategoriaApi {
    @GET("categorias")
    suspend fun list(): List<Categoria>

    @POST("categorias")
    suspend fun add(@Body categoria: Categoria): Categoria
}