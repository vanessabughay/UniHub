package com.example.unihub.data.api

import com.example.unihub.data.model.Quadro
import retrofit2.Response
import retrofit2.http.*

interface QuadroApi {
    @GET("quadros-planejamento")
    suspend fun getQuadros(): Response<List<Quadro>>

    @GET("quadros-planejamento/{quadroId}")
    suspend fun getQuadroById(@Path("quadroId") quadroId: Long): Response<Quadro>

    @POST("quadros-planejamento")
    suspend fun addQuadro(@Body quadro: Quadro): Response<Quadro>

    @PUT("quadros-planejamento/{quadroId}")
    suspend fun updateQuadro(
        @Path("quadroId") quadroId: Long,
        @Body quadro: Quadro
    ): Response<Quadro>


    @DELETE("quadros-planejamento/{quadroId}")
    suspend fun deleteQuadro(@Path("quadroId") quadroId: Long): Response<Void>
}