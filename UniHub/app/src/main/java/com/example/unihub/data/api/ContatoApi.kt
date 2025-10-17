package com.example.unihub.data.api

import com.example.unihub.data.model.Contato
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ContatoApi {
    @GET("contato")
    suspend fun list(): List<Contato>

    @GET("contato/{id}")
    suspend fun get(@Path("id") id: Long): Contato

    @POST("contato")
    suspend fun add(@Body contato: Contato): Contato

    @PUT("contato/{id}")
    suspend fun update(@Path("id") id: Long, @Body contato: Contato): Contato

    @DELETE("contato/{id}")
    suspend fun delete(@Path("id") id: Long)

    @GET("contato/pendentes")
    suspend fun listPendentes(@Query("email") email: String): List<Contato>

    @POST("contato/pendentes/{id}/aceitar")
    suspend fun acceptInvite(@Path("id") id: Long)

    @POST("contato/pendentes/{id}/rejeitar")
    suspend fun rejectInvite(@Path("id") id: Long)
}