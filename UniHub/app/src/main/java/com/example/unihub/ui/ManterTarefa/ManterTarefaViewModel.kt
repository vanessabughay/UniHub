package com.example.unihub.ui.ManterTarefa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Status
import com.example.unihub.data.model.Tarefa
import com.example.unihub.data.repository.TarefaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TarefaFormUiState(
    val isLoading: Boolean = false,
    val tarefa: Tarefa? = null,
    val errorMessage: String? = null,
    val operationCompleted: Boolean = false
)

class TarefaFormViewModel(private val repository: TarefaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TarefaFormUiState())
    val uiState: StateFlow<TarefaFormUiState> = _uiState.asStateFlow()


    fun carregarTarefa(colunaId: String, tarefaId: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val tarefaCarregada = repository.getTarefa(colunaId, tarefaId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tarefa = tarefaCarregada,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tarefa = null,
                        errorMessage = e.message ?: "Erro ao carregar tarefa."
                    )
                }
            }
        }
    }

    fun cadastrarTarefa(quadroId: String, colunaId: String, novaTarefa: Tarefa) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, operationCompleted = false) }
        viewModelScope.launch {
            try {
                repository.createTarefa(quadroId, colunaId, novaTarefa)
                _uiState.update { it.copy(isLoading = false, operationCompleted = true) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Erro ao salvar tarefa.",
                        operationCompleted = false
                    )
                }
            }
        }
    }

    fun atualizarTarefa(colunaId: String, tarefaAtualizada: Tarefa) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, operationCompleted = false) }
        viewModelScope.launch {
            try {
                val tarefaAntiga = _uiState.value.tarefa
                var tarefaParaSalvar = tarefaAtualizada

                if (tarefaAtualizada.status == Status.CONCLUIDA && tarefaAntiga?.dataFim == null) {
                    tarefaParaSalvar = tarefaAtualizada.copy(dataFim = System.currentTimeMillis())

            } else if (tarefaAntiga?.status == Status.CONCLUIDA && tarefaAtualizada.status != Status.CONCLUIDA) {
                tarefaParaSalvar = tarefaAtualizada.copy(dataFim = null)
                }

            val tarefaSalva = repository.updateTarefa(colunaId, tarefaParaSalvar)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    tarefa = tarefaSalva,
                    operationCompleted = true
                )
            }
        } catch (e: Exception) {
                e.printStackTrace()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Erro ao atualizar tarefa.",
                    operationCompleted = false
                )
            }
            }
        }
    }

    fun excluirTarefa(colunaId: String, tarefaId: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, operationCompleted = false) }
        viewModelScope.launch {
            try {
                repository.deleteTarefa(colunaId, tarefaId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tarefa = null,
                        operationCompleted = true
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Erro ao excluir tarefa.",
                        operationCompleted = false
                    )
                }
            }
        }
    }

fun resetOperationResult() {
    _uiState.update { it.copy(operationCompleted = false) }
}

fun clearError() {
    _uiState.update { it.copy(errorMessage = null) }
}
}