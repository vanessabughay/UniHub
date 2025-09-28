package com.example.unihub.data.api

import com.example.unihub.data.model.QuadroDePlanejamento
import retrofit2.http.*

interface QuadroApi {
    @GET("quadros-planejamento")
    suspend fun getQuadros(): List<QuadroDePlanejamento>

    @GET("quadros-planejamento/{quadroId}")
    suspend fun getQuadroById(@Path("quadroId") quadroId: String): QuadroDePlanejamento

    @POST("quadros-planejamento")
    suspend fun addQuadro(@Body quadro: QuadroDePlanejamento): QuadroDePlanejamento

    @PUT("quadros-planejamento/{quadroId}")
    suspend fun updateQuadro(
        @Path("quadroId") quadroId: String,
        @Body quadro: QuadroDePlanejamento
    ): QuadroDePlanejamento


    @DELETE("quadros-planejamento/{quadroId}")
    suspend fun deleteQuadro(@Path("quadroId") quadroId: String)
}