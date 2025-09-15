package com.example.unihub.data.repository

import com.example.unihub.data.model.QuadroDePlanejamento
import retrofit2.http.*

interface QuadroApi {
    @GET("quadros")
    suspend fun getQuadros(): List<QuadroDePlanejamento>

    @GET("quadros/{quadroId}")
    suspend fun getQuadroById(@Path("quadroId") quadroId: String): QuadroDePlanejamento

    @POST("quadros")
    suspend fun addQuadro(@Body quadro: QuadroDePlanejamento): QuadroDePlanejamento

    @PUT("quadros/{quadroId}")
    suspend fun updateQuadro(@Path("quadroId") quadroId: String, @Body quadro: QuadroDePlanejamento): QuadroDePlanejamento

    @DELETE("quadros/{quadroId}")
    suspend fun deleteQuadro(@Path("quadroId") quadroId: String)
}