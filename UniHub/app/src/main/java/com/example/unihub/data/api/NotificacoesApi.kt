package com.example.unihub.data.api

import com.example.unihub.data.model.NotificacoesConfig
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface NotificacoesApi {

    @GET("api/notificacoes/config")
    suspend fun carregar(): NotificacoesConfig

    @PUT("api/notificacoes/config")
    suspend fun salvar(
        @Body config: NotificacoesConfig
    ): NotificacoesConfig // O backend retorna a configuração salva
}