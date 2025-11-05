package com.example.unihub.data.api

import com.example.unihub.data.model.NotificationLogRequest
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface NotificationLogApi {
    @POST("api/notificacoes/historico")
    suspend fun logNotification(
        @Header("Authorization") authHeader: String,
        @Body request: NotificationLogRequest
    )
}