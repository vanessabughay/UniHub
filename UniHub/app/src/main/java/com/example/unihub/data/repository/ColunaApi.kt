package com.example.unihub.data.repository

import com.example.unihub.data.model.Coluna
import retrofit2.http.*

interface ColunaApi {

    @GET("colunas")
    suspend fun getColunas(): List<Coluna>

    @GET("colunas/{colunaId}")
    suspend fun getColunaById(@Path("colunaId") colunaId: String): Coluna

    @POST("colunas")
    suspend fun addColuna(@Body coluna: Coluna): Coluna

    @PUT("colunas/{colunaId}")
    suspend fun updateColuna(@Path("colunaId") colunaId: String, @Body coluna: Coluna): Coluna

    @DELETE("colunas/{colunaId}")
    suspend fun deleteColuna(@Path("colunaId") colunaId: String)
}