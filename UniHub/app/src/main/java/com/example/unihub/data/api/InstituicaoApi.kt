package com.example.unihub.data.api

import com.example.unihub.data.model.Instituicao
import retrofit2.http.*

interface InstituicaoApi {
    @GET("instituicoes")
    suspend fun list(@Query("nome") nome: String?): List<Instituicao>

    @POST("instituicoes")
    suspend fun add(@Body instituicao: Instituicao): Instituicao

    @PUT("instituicoes/{id}")
    suspend fun update(@Path("id") id: Long, @Body instituicao: Instituicao): Instituicao
}