package com.example.unihub.data.repository

import com.example.unihub.data.api.TarefaApi
import com.example.unihub.data.dto.TarefaPlanejamentoRequestDto
import com.example.unihub.data.model.Tarefa
import java.time.Instant
import java.time.ZoneId

open class TarefaRepository(private val apiService: TarefaApi) {

    suspend fun getTarefa(colunaId: String, tarefaId: String): Tarefa {
        return apiService.getTarefa(colunaId, tarefaId)
    }

    suspend fun createTarefa(quadroId: String, colunaId: String, tarefa: Tarefa) {
        val request = tarefa.toPlanejamentoRequest()
        apiService.createTarefa(quadroId, colunaId, request)
    }

    suspend fun updateTarefa(colunaId: String, tarefa: Tarefa): Tarefa {
        // A API precisa do ID da tarefa na URL, então usamos 'tarefa.id'
        return apiService.updateTarefa(colunaId, tarefa.id, tarefa)
    }

    suspend fun deleteTarefa(colunaId: String, tarefaId: String) {
        apiService.deleteTarefa(colunaId, tarefaId)
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
        responsavelId = null
    )
}