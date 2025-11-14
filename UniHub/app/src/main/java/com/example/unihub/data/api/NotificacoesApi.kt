package com.example.unihub.data.api

import com.example.unihub.data.model.NotificacoesConfig
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotificacoesApi {

    @GET("api/usuarios/{usuarioId}/notificacoes-config")
    suspend fun carregar(
        @Path("usuarioId") usuarioId: Long
    ): NotificacoesConfig


    @PUT("api/usuarios/{usuarioId}/notificacoes-config")
    suspend fun salvar(
        @Path("usuarioId") usuarioId: Long,
        @Body config: NotificacoesConfig
    ): NotificacoesConfig // O backend retorna a configuração salva
}