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

    fun carregarTarefa(colunaId: String, tarefaId: String) {
        viewModelScope.launch {
            try {
                val tarefaCarregada = repository.getTarefa(colunaId, tarefaId)
                _tarefaState.value = tarefaCarregada
            } catch (e: Exception) {
                e.printStackTrace()
                _tarefaState.value = null
            }
        }
    }

    fun cadastrarTarefa(colunaId: String, novaTarefa: Tarefa) {
        viewModelScope.launch {
            try {
                repository.createTarefa(colunaId, novaTarefa)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun atualizarTarefa(colunaId: String, tarefaAtualizada: Tarefa) {
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

                repository.updateTarefa(colunaId, tarefaParaSalvar)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun excluirTarefa(colunaId: String, tarefaId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTarefa(colunaId, tarefaId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}