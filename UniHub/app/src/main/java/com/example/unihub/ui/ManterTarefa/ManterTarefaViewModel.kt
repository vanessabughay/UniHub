package com.example.unihub.ui.ManterTarefa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Status
import com.example.unihub.data.model.Tarefa
import com.example.unihub.data.repository.TarefaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TarefaFormResult {
    object Idle : TarefaFormResult()
    object Success : TarefaFormResult()
    data class Error(val message: String) : TarefaFormResult()
}

class TarefaFormViewModel(
    private val repository: TarefaRepository
) : ViewModel() {

    private val _tarefaState = MutableStateFlow<Tarefa?>(null)
    val tarefa: StateFlow<Tarefa?> = _tarefaState.asStateFlow()

    private val _formResult = MutableStateFlow<TarefaFormResult>(TarefaFormResult.Idle)
    val formResult: StateFlow<TarefaFormResult> = _formResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    fun carregarTarefa(colunaId: String, tarefaId: String) {
        // _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val tarefaCarregada = repository.getTarefa(colunaId, tarefaId)
                _tarefaState.value = tarefaCarregada
            } catch (e: Exception) {
                _formResult.value = TarefaFormResult.Error(e.message ?: "Erro ao carregar tarefa.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cadastrarTarefa(quadroId: String, colunaId: String, novaTarefa: Tarefa) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.createTarefa(quadroId, colunaId, novaTarefa)
                _formResult.value = TarefaFormResult.Success
            } catch (e: Exception) {
                _formResult.value = TarefaFormResult.Error(e.message ?: "Erro ao salvar tarefa.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun atualizarTarefa(colunaId: String, tarefaAtualizada: Tarefa) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val tarefaAtual = _tarefaState.value
                var tarefaParaSalvar = tarefaAtualizada

                tarefaParaSalvar = if (tarefaAtualizada.status == Status.CONCLUIDA && tarefaAtual?.dataFim == null) {
                    tarefaAtualizada.copy(dataFim = System.currentTimeMillis())
                } else if (tarefaAtual?.status == Status.CONCLUIDA && tarefaAtualizada.status != Status.CONCLUIDA) {
                    tarefaAtualizada.copy(dataFim = null)
                } else {
                    tarefaAtualizada
                }

                val tarefaSalva = repository.updateTarefa(colunaId, tarefaParaSalvar)
                _tarefaState.value = tarefaSalva
                _formResult.value = TarefaFormResult.Success
            } catch (e: Exception) {
                _formResult.value = TarefaFormResult.Error(e.message ?: "Erro ao atualizar tarefa.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun excluirTarefa(colunaId: String, tarefaId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteTarefa(colunaId, tarefaId)
                _tarefaState.value = null
                _formResult.value = TarefaFormResult.Success
            } catch (e: Exception) {
                _formResult.value = TarefaFormResult.Error(e.message ?: "Erro ao excluir tarefa.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetFormResult() {
        _formResult.value = TarefaFormResult.Idle
    }
}
