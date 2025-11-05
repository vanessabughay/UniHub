package com.example.unihub.data.repository

import com.example.unihub.data.api.TarefaApi
import com.example.unihub.data.dto.AtualizarTarefaPlanejamentoRequestDto
import com.example.unihub.data.dto.TarefaNotificacaoRequestDto
import com.example.unihub.data.dto.ComentarioRequestDto
import com.example.unihub.data.dto.TarefaPlanejamentoRequestDto
import com.example.unihub.data.model.Comentario
import com.example.unihub.data.model.Tarefa
import com.example.unihub.data.model.TarefaPreferenciaResponse
import com.example.unihub.data.model.ComentariosResponse
import com.example.unihub.data.dto.TarefaDto

open class TarefaRepository(private val apiService: TarefaApi) {

    suspend fun getTarefa(quadroId: String, colunaId: String, tarefaId: String): Tarefa {
        return apiService.getTarefa(quadroId, colunaId, tarefaId)
    }

    suspend fun createTarefa(quadroId: String, colunaId: String, tarefa: Tarefa) {
        val request = tarefa.toPlanejamentoRequest()
        apiService.createTarefa(quadroId, colunaId, request)
    }

    suspend fun updateTarefa(quadroId: String, colunaId: String, tarefa: Tarefa): Tarefa {
        // A API precisa do ID da tarefa na URL, então usamos 'tarefa.id'
        val request = tarefa.toAtualizarRequest()
        return apiService.updateTarefa(quadroId, colunaId, tarefa.id, request)
    }

    suspend fun deleteTarefa(quadroId: String, colunaId: String, tarefaId: String) {
        apiService.deleteTarefa(quadroId, colunaId, tarefaId)
    }

    open suspend fun carregarComentarios(quadroId: String, colunaId: String, tarefaId: String): ComentariosResponse {
        return apiService.getComentarios(quadroId, colunaId, tarefaId)
    }

    open suspend fun criarComentario(
        quadroId: String,
        colunaId: String,
        tarefaId: String,
        conteudo: String
    ): Comentario {
        return apiService.createComentario(quadroId, colunaId, tarefaId, ComentarioRequestDto(conteudo))
    }

    open suspend fun atualizarComentario(
        quadroId: String,
        colunaId: String,
        tarefaId: String,
        comentarioId: String,
        conteudo: String
    ): Comentario {
        return apiService.updateComentario(
            quadroId,
            colunaId,
            tarefaId,
            comentarioId,
            ComentarioRequestDto(conteudo)
        )
    }

    open suspend fun excluirComentario(
        quadroId: String,
        colunaId: String,
        tarefaId: String,
        comentarioId: String
    ) {
        apiService.deleteComentario(quadroId, colunaId, tarefaId, comentarioId)
    }

    open suspend fun atualizarPreferenciaTarefa(
        quadroId: String,
        colunaId: String,
        tarefaId: String,
        receberNotificacoes: Boolean
    ): TarefaPreferenciaResponse {
        return apiService.updateTarefaPreference(
            quadroId,
            colunaId,
            tarefaId,
            TarefaNotificacaoRequestDto(receberNotificacoes)
        )
    }

    open suspend fun getProximasTarefas(): List<TarefaDto> {
        return apiService.getProximasTarefas()
    }

}

private fun Tarefa.toPlanejamentoRequest(): TarefaPlanejamentoRequestDto {

    return TarefaPlanejamentoRequestDto(
        titulo = this.titulo,
        descricao = this.descricao,
        dataPrazo = this.prazo?.takeUnless { it.isBlank() },
        responsavelIds = this.responsaveisIds
    )
}

private fun Tarefa.toAtualizarRequest(): AtualizarTarefaPlanejamentoRequestDto {
    return AtualizarTarefaPlanejamentoRequestDto(
        titulo = this.titulo,
        descricao = this.descricao,
        status = this.status.name,
        prazo = this.prazo?.takeUnless { it.isBlank() },
        responsavelIds = this.responsaveisIds
    )
}