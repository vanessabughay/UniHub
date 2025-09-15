package com.example.unihub.data.repository

import com.example.unihub.data.model.Coluna
import retrofit2.http.*

interface ColunaApi {
    @GET("quadros/{quadroId}/colunas")
    suspend fun getColunas(@Path("quadroId") quadroId: String): List<Coluna>

    @GET("quadros/{quadroId}/colunas/{colunaId}")
    suspend fun getColunaById(@Path("quadroId") quadroId: String, @Path("colunaId") colunaId: String): Coluna

    @POST("quadros/{quadroId}/colunas")
    suspend fun addColuna(@Path("quadroId") quadroId: String, @Body coluna: Coluna): Coluna

    @PUT("quadros/{quadroId}/colunas/{colunaId}")
    suspend fun updateColuna(@Path("quadroId") quadroId: String, @Path("colunaId") colunaId: String, @Body coluna: Coluna): Coluna

    @DELETE("quadros/{quadroId}/colunas/{colunaId}")
    suspend fun deleteColuna(@Path("quadroId") quadroId: String, @Path("colunaId") colunaId: String)
}