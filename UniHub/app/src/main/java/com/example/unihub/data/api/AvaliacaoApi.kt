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

interface AvaliacaoApi {
    @GET("api/avaliacoes") // Corresponde a @GetMapping em AvaliacaoController
    suspend fun list(): Response<List<Avaliacao>>

    @GET("api/avaliacoes/{id}") // Corresponde a @GetMapping("/{id}")
    suspend fun get(@Path("id") id: Long): Response<Avaliacao>

    @POST("api/avaliacoes") // Corresponde a @PostMapping
    suspend fun add(@Body body: AvaliacaoRequestDto): Response<Avaliacao>

    @PUT("api/avaliacoes/{id}") // Corresponde a @PutMapping("/{id}")
    suspend fun update(@Path("id") id: Long, @Body body: AvaliacaoRequestDto): Response<Avaliacao>

    @DELETE("api/avaliacoes/{id}") // Corresponde a @DeleteMapping("/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Void>


}
