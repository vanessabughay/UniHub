package com.example.unihub.data.apiBackend

import com.example.unihub.data.api.CompartilhamentoApi
import com.example.unihub.data.config.RetrofitClient
import com.example.unihub.data.model.CompartilharDisciplinaRequest
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.model.ConviteCompartilhamentoResponse
import com.example.unihub.data.model.NotificacaoResponse
import com.example.unihub.data.model.UsuarioResumo
import com.example.unihub.data.repository.CompartilhamentoBackend

class ApiCompartilhamentoBackend : CompartilhamentoBackend {

    private val api: CompartilhamentoApi by lazy {
        RetrofitClient.create(CompartilhamentoApi::class.java)
    }

    private fun authHeader(): String = TokenManager.token?.takeIf { it.isNotBlank() }
        ?.let { "Bearer $it" }
        ?: throw IllegalStateException("Token de autenticação indisponível")


    override suspend fun listarContatos(usuarioId: Long): List<UsuarioResumo> =
        api.listarContatos(authHeader(), usuarioId)

    override suspend fun compartilhar(request: CompartilharDisciplinaRequest): ConviteCompartilhamentoResponse =
        api.compartilhar(authHeader(), request)

    override suspend fun aceitarConvite(
        conviteId: Long,
        usuarioId: Long
    ): ConviteCompartilhamentoResponse =
        api.aceitarConvite(authHeader(), conviteId, CompartilhamentoApi.AcaoConviteRequest(usuarioId))
    override suspend fun rejeitarConvite(
        conviteId: Long,
        usuarioId: Long
    ): ConviteCompartilhamentoResponse =
        api.rejeitarConvite(authHeader(), conviteId, CompartilhamentoApi.AcaoConviteRequest(usuarioId))

    override suspend fun listarNotificacoes(usuarioId: Long): List<NotificacaoResponse> =
        api.listarNotificacoes(authHeader(), usuarioId)
}