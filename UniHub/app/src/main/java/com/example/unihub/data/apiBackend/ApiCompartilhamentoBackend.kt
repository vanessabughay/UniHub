package com.example.unihub.data.apiBackend

import com.example.unihub.data.api.CompartilhamentoApi
import com.example.unihub.data.config.RetrofitClient
import com.example.unihub.data.model.CompartilharDisciplinaRequest
import com.example.unihub.data.model.ConviteCompartilhamentoResponse
import com.example.unihub.data.model.NotificacaoResponse
import com.example.unihub.data.model.UsuarioResumo
import com.example.unihub.data.repository.CompartilhamentoBackend

class ApiCompartilhamentoBackend : CompartilhamentoBackend {

    private val api: CompartilhamentoApi by lazy {
        RetrofitClient.create(CompartilhamentoApi::class.java)
    }

    override suspend fun listarContatos(usuarioId: Long): List<UsuarioResumo> =
        api.listarContatos(usuarioId)

    override suspend fun compartilhar(request: CompartilharDisciplinaRequest): ConviteCompartilhamentoResponse =
        api.compartilhar(request)

    override suspend fun aceitarConvite(
        conviteId: Long,
        usuarioId: Long
    ): ConviteCompartilhamentoResponse =
        api.aceitarConvite(conviteId, CompartilhamentoApi.AcaoConviteRequest(usuarioId))
    override suspend fun rejeitarConvite(
        conviteId: Long,
        usuarioId: Long
    ): ConviteCompartilhamentoResponse =
        api.rejeitarConvite(conviteId, CompartilhamentoApi.AcaoConviteRequest(usuarioId))

    override suspend fun listarNotificacoes(usuarioId: Long): List<NotificacaoResponse> =
        api.listarNotificacoes(usuarioId)
}