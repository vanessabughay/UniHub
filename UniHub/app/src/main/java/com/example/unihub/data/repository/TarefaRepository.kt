package com.example.unihub.data.repository

import com.example.unihub.data.api.TarefaApi
import com.example.unihub.data.dto.AtualizarTarefaPlanejamentoRequestDto
import com.example.unihub.data.dto.TarefaPlanejamentoRequestDto
import com.example.unihub.data.model.Tarefa
import java.time.Instant
import java.time.ZoneId

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

}

private fun Tarefa.toPlanejamentoRequest(): TarefaPlanejamentoRequestDto {
    val prazoLocalDate = Instant.ofEpochMilli(this.prazo)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    return TarefaPlanejamentoRequestDto(
        titulo = this.titulo,
        descricao = this.descricao,
        dataPrazo = prazoLocalDate,
        responsavelIds = this.responsaveisIds
    )
}

private fun Tarefa.toAtualizarRequest(): AtualizarTarefaPlanejamentoRequestDto {
    return AtualizarTarefaPlanejamentoRequestDto(
        titulo = this.titulo,
        descricao = this.descricao,
        status = this.status.name,
        prazo = this.prazo,
        dataInicio = this.dataInicio,
        dataFim = this.dataFim,
        responsavelIds = this.responsaveisIds
    )
}