package com.example.unihub.data.repository

import com.example.unihub.data.apiBackend.ApiAnotacoesBackend
import com.example.unihub.data.dto.AnotacoesRequest
import com.example.unihub.data.model.Anotacao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AnotacoesRepository(
    private val api: ApiAnotacoesBackend = ApiAnotacoesBackend()
) {

    fun listar(disciplinaId: Long): Flow<List<Anotacao>> = flow {
        val anotacoes = api.listAnotacoesApi(disciplinaId)
        emit(anotacoes)
    }

    suspend fun obter(disciplinaId: Long, id: Long): Anotacao? {
        return api.getAnotacaoByIdApi(disciplinaId, id)
    }

    suspend fun criar(disciplinaId: Long, titulo: String, conteudo: String): Anotacao {
        val request = AnotacoesRequest(titulo = titulo, conteudo = conteudo)
        return api.addAnotacaoApi(disciplinaId, request)
    }

    suspend fun atualizar(disciplinaId: Long, id: Long, titulo: String, conteudo: String): Anotacao {
        val request = AnotacoesRequest(titulo = titulo, conteudo = conteudo)
        return api.updateAnotacaoApi(disciplinaId, id, request)
    }

    suspend fun excluir(disciplinaId: Long, id: Long) {
        api.deleteAnotacaoApi(disciplinaId, id)
    }
}