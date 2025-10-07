package com.example.unihub.data.api

import com.example.unihub.data.dto.AvaliacaoRequestDto
import com.example.unihub.data.model.Avaliacao
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AvaliacaoApi {
    @GET("api/avaliacoes")
    suspend fun list(): Response<List<Avaliacao>>

    @GET("api/avaliacoes")
    suspend fun listPorDisciplina(@Query("disciplinaId") disciplinaId: Long): Response<List<Avaliacao>>


    @GET("api/avaliacoes/{id}")
    suspend fun get(@Path("id") id: Long): Response<Avaliacao>

    @POST("api/avaliacoes")
    suspend fun add(@Body body: AvaliacaoRequestDto): Response<Void>

    @PUT("api/avaliacoes/{id}")
    suspend fun update(@Path("id") id: Long, @Body body: AvaliacaoRequestDto): Response<Void>

    @DELETE("api/avaliacoes/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Void>
}
