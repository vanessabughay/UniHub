package com.example.unihub.data.api

import com.example.unihub.data.model.Ausencia
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AusenciaApi {
    @GET("ausencias")
    suspend fun list(): List<Ausencia>

    @GET("ausencias/{id}")
    suspend fun get(@Path("id") id: Long): Ausencia

    @POST("ausencias")
    suspend fun add(@Body ausencia: Ausencia): Ausencia

    @PUT("ausencias/{id}")
    suspend fun update(@Path("id") id: Long, @Body ausencia: Ausencia): Ausencia

    @DELETE("ausencias/{id}")
    suspend fun delete(@Path("id") id: Long)
}