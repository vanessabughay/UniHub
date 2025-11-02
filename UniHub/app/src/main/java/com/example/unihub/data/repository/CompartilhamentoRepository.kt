package com.example.unihub.data.repository

import com.example.unihub.data.model.CompartilharDisciplinaRequest
import com.example.unihub.data.model.ConviteCompartilhamentoResponse
import com.example.unihub.data.model.NotificacaoResponse
import com.example.unihub.data.model.UsuarioResumo

interface CompartilhamentoBackend {
    suspend fun listarContatos(usuarioId: Long): List<UsuarioResumo>
    suspend fun compartilhar(request: CompartilharDisciplinaRequest): ConviteCompartilhamentoResponse
    suspend fun aceitarConvite(conviteId: Long, usuarioId: Long): ConviteCompartilhamentoResponse
    suspend fun rejeitarConvite(conviteId: Long, usuarioId: Long): ConviteCompartilhamentoResponse
    suspend fun listarNotificacoes(usuarioId: Long): List<NotificacaoResponse>
}

class CompartilhamentoRepository(private val backend: CompartilhamentoBackend) {

    suspend fun listarContatos(usuarioId: Long): List<UsuarioResumo> = backend.listarContatos(usuarioId)

    suspend fun compartilhar(request: CompartilharDisciplinaRequest): ConviteCompartilhamentoResponse =
        backend.compartilhar(request)

    suspend fun aceitarConvite(conviteId: Long, usuarioId: Long): ConviteCompartilhamentoResponse =
        backend.aceitarConvite(conviteId, usuarioId)

    suspend fun rejeitarConvite(conviteId: Long, usuarioId: Long): ConviteCompartilhamentoResponse =
        backend.rejeitarConvite(conviteId, usuarioId)

    suspend fun listarNotificacoes(usuarioId: Long): List<NotificacaoResponse> =
        backend.listarNotificacoes(usuarioId)
}