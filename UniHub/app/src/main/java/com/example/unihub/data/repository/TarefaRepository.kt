package com.example.unihub.data.repository

import com.example.unihub.data.api.TarefaApi
import com.example.unihub.data.model.Tarefa

open class TarefaRepository(private val apiService: TarefaApi) {

    suspend fun getTarefa(quadroId: String, colunaId: String, tarefaId: String): Tarefa {
        return apiService.getTarefa(quadroId, colunaId, tarefaId)
    }

    suspend fun createTarefa(quadroId: String, colunaId: String, tarefa: Tarefa): Tarefa {
        return apiService.createTarefa(quadroId, colunaId, tarefa)
    }

    suspend fun updateTarefa(quadroId: String, colunaId: String, tarefa: Tarefa): Tarefa {
        // A API precisa do ID da tarefa na URL, então usamos 'tarefa.id'
        return apiService.updateTarefa(quadroId, colunaId, tarefa.id, tarefa)
    }

    suspend fun deleteTarefa(quadroId: String, colunaId: String, tarefaId: String) {
        apiService.deleteTarefa(quadroId, colunaId, tarefaId)
    }
}