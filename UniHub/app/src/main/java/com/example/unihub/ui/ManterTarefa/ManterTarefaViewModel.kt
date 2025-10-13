package com.example.unihub.ui.ManterTarefa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Status
import com.example.unihub.data.model.Tarefa
import com.example.unihub.data.repository.TarefaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TarefaFormViewModel(private val repository: TarefaRepository) : ViewModel() {

    private val _tarefaState = MutableStateFlow<Tarefa?>(null)
    val tarefa: StateFlow<Tarefa?> = _tarefaState

    fun carregarTarefa(quadroId: String, colunaId: String, tarefaId: String) {
        viewModelScope.launch {
            try {
                val tarefaCarregada = repository.getTarefa(quadroId, colunaId, tarefaId)
                _tarefaState.value = tarefaCarregada
            } catch (e: Exception) {
                e.printStackTrace()
                _tarefaState.value = null
            }
        }
    }

    fun cadastrarTarefa(quadroId: String, colunaId: String, novaTarefa: Tarefa) {
        viewModelScope.launch {
            try {
                repository.createTarefa(quadroId, colunaId, novaTarefa)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun atualizarTarefa(quadroId: String, colunaId: String, tarefaAtualizada: Tarefa) {
        viewModelScope.launch {
            try {
                val tarefaAntiga = _tarefaState.value
                var tarefaParaSalvar = tarefaAtualizada

                if (tarefaAtualizada.status == Status.CONCLUIDA && tarefaAntiga?.dataFim == null) {
                    tarefaParaSalvar = tarefaAtualizada.copy(dataFim = System.currentTimeMillis())
                }
                else if (tarefaAntiga?.status == Status.CONCLUIDA && tarefaAtualizada.status != Status.CONCLUIDA) {
                    tarefaParaSalvar = tarefaAtualizada.copy(dataFim = null)
                }

                fun atualizarTarefa(quadroId: String, colunaId: String, tarefaAtualizada: Tarefa) {
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun excluirTarefa(quadroId: String, colunaId: String, tarefaId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTarefa(quadroId, colunaId, tarefaId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}