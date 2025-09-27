package com.example.unihub.data.api

import com.example.unihub.data.model.Coluna
import retrofit2.http.*

interface ColunaApi {
    @GET("quadros-planejamento/{quadroId}/colunas")
    suspend fun getColunas(@Path("quadroId") quadroId: String): List<Coluna>

    @GET("quadros-planejamento/{quadroId}/colunas/{colunaId}")
    suspend fun getColunaById(@Path("quadroId") quadroId: String, @Path("colunaId") colunaId: String): Coluna

    @POST("quadros-planejamento/{quadroId}/colunas")
    suspend fun addColuna(@Path("quadroId") quadroId: String, @Body coluna: Coluna): Coluna

    @PUT("quadros-planejamento/{quadroId}/colunas/{colunaId}")
    suspend fun updateColuna(@Path("quadroId") quadroId: String, @Path("colunaId") colunaId: String, @Body coluna: Coluna): Coluna

    @DELETE("quadros-planejamento/{quadroId}/colunas/{colunaId}")
    suspend fun deleteColuna(@Path("quadroId") quadroId: String, @Path("colunaId") colunaId: String)
}