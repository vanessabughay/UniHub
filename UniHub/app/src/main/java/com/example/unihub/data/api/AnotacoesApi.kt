package com.example.unihub.data.api

import com.example.unihub.data.dto.PageResponse
import com.example.unihub.data.dto.AnotacoesDTO
import com.example.unihub.data.dto.AnotacoesRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AnotacoesApi {

    @GET("api/disciplinas/{disciplinaId}/anotacoes")
    suspend fun listar(
        @Path("disciplinaId") disciplinaId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 500,
        @Query("sort") sort: String? = null // ex.: "createdAt,desc"
    ): PageResponse<AnotacoesDTO>

    @GET("api/disciplinas/{disciplinaId}/anotacoes/{id}")
    suspend fun obter(
        @Path("disciplinaId") disciplinaId: Long,
        @Path("id") id: Long
    ): AnotacoesDTO

    @POST("api/disciplinas/{disciplinaId}/anotacoes")
    suspend fun criar(
        @Path("disciplinaId") disciplinaId: Long,
        @Body body: AnotacoesRequest
    ): AnotacoesDTO

    @PUT("api/disciplinas/{disciplinaId}/anotacoes/{id}")
    suspend fun atualizar(
        @Path("disciplinaId") disciplinaId: Long,
        @Path("id") id: Long,
        @Body body: AnotacoesRequest
    ): AnotacoesDTO

    @DELETE("api/disciplinas/{disciplinaId}/anotacoes/{id}")
    suspend fun excluir(
        @Path("disciplinaId") disciplinaId: Long,
        @Path("id") id: Long
    )
}