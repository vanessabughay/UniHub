package com.example.unihub.data.repository

import com.example.unihub.data.model.CompartilharDisciplinaRequest
import com.example.unihub.data.model.ConviteCompartilhamentoResponse
import com.example.unihub.data.model.NotificacaoResponse
import com.example.unihub.data.model.UsuarioResumo
import com.example.unihub.data.remote.CompartilhamentoApiService

class CompartilhamentoRepository(
    private val api: CompartilhamentoApiService
) {

    suspend fun listarContatos(usuarioId: Long): List<UsuarioResumo> {
        return api.listarContatos(usuarioId)
    }

    suspend fun compartilharDisciplina(request: CompartilharDisciplinaRequest): ConviteCompartilhamentoResponse {
        return api.compartilhar(request)
    }

    suspend fun aceitarConvite(conviteId: Long, usuarioId: Long): ConviteCompartilhamentoResponse {
        return api.aceitarConvite(conviteId, CompartilhamentoApiService.AcaoConviteRequest(usuarioId))
    }

    suspend fun rejeitarConvite(conviteId: Long, usuarioId: Long): ConviteCompartilhamentoResponse {
        return api.rejeitarConvite(conviteId, CompartilhamentoApiService.AcaoConviteRequest(usuarioId))
    }

    suspend fun listarNotificacoes(usuarioId: Long): List<NotificacaoResponse> {
        return api.listarNotificacoes(usuarioId)
    }
}
