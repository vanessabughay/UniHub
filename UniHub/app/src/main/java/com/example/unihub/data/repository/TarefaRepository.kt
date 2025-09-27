package com.example.unihub.data.repository

import com.example.unihub.data.api.TarefaApi
import com.example.unihub.data.model.Tarefa

open class TarefaRepository(private val apiService: TarefaApi) {

    suspend fun getTarefa(colunaId: String, tarefaId: String): Tarefa {
        return apiService.getTarefa(colunaId, tarefaId)
    }

    suspend fun createTarefa(colunaId: String, tarefa: Tarefa): Tarefa {
        return apiService.createTarefa(colunaId, tarefa)
    }

    suspend fun updateTarefa(colunaId: String, tarefa: Tarefa): Tarefa {
        // A API precisa do ID da tarefa na URL, então usamos 'tarefa.id'
        return apiService.updateTarefa(colunaId, tarefa.id, tarefa)
    }

    suspend fun deleteTarefa(colunaId: String, tarefaId: String) {
        apiService.deleteTarefa(colunaId, tarefaId)
    }
}