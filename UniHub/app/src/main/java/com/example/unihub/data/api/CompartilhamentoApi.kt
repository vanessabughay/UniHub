package com.example.unihub.data.api

import com.example.unihub.data.model.CompartilharDisciplinaRequest
import com.example.unihub.data.model.ConviteCompartilhamentoResponse
import com.example.unihub.data.model.NotificacaoResponse
import com.example.unihub.data.model.UsuarioResumo
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface CompartilhamentoApi {

    @GET("usuarios/{usuarioId}/contatos")
    suspend fun listarContatos(
        @Header("Authorization") authHeader: String,
        @Path("usuarioId") usuarioId: Long
    ): List<UsuarioResumo>

    @POST("usuarios/{usuarioId}/contatos")
    suspend fun adicionarContato(
        @Path("usuarioId") usuarioId: Long,
        @Body request: AdicionarContatoRequest
    ): UsuarioResumo

    @POST("compartilhamentos/convites")
    suspend fun compartilhar(
        @Header("Authorization") authHeader: String,
        @Body request: CompartilharDisciplinaRequest
    ): ConviteCompartilhamentoResponse


    @POST("compartilhamentos/convites/{conviteId}/aceitar")
    suspend fun aceitarConvite(
        @Header("Authorization") authHeader: String,
        @Path("conviteId") conviteId: Long,
        @Body request: AcaoConviteRequest
    ): ConviteCompartilhamentoResponse

    @POST("compartilhamentos/convites/{conviteId}/rejeitar")
    suspend fun rejeitarConvite(
        @Header("Authorization") authHeader: String,
        @Path("conviteId") conviteId: Long,
        @Body request: AcaoConviteRequest
    ): ConviteCompartilhamentoResponse

    @GET("usuarios/{usuarioId}/notificacoes")
    suspend fun listarNotificacoes(
        @Header("Authorization") authHeader: String,
        @Path("usuarioId") usuarioId: Long
    ): List<NotificacaoResponse>

    data class AdicionarContatoRequest(val contatoId: Long)

    data class AcaoConviteRequest(val usuarioId: Long)
}