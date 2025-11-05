package com.example.unihub.ui.VisualizarQuadro

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Coluna
import com.example.unihub.data.model.Quadro
import com.example.unihub.data.model.Status
import com.example.unihub.data.model.Tarefa
import com.example.unihub.data.repository.QuadroRepository
import com.example.unihub.data.repository.TarefaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VisualizarQuadroUiState(
    val isLoading: Boolean = false,
    val quadro: Quadro? = null,
    val colunas: List<Coluna> = emptyList(),
    val error: String? = null
)

class VisualizarQuadroViewModel(
    private val quadroRepository: QuadroRepository,
    private val tarefaRepository: TarefaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisualizarQuadroUiState())
    val uiState: StateFlow<VisualizarQuadroUiState> = _uiState.asStateFlow()

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun carregarQuadro(quadroId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val quadroCarregado = quadroRepository.getQuadroById(quadroId)
                if (quadroCarregado != null) {
                    _uiState.update {
                        it.copy(
                            quadro = quadroCarregado,
                            colunas = quadroCarregado.colunas.sortedBy { it.ordem },
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Quadro não encontrado.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Erro ao carregar quadro.") }
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun atualizarStatusTarefa(
        quadroId: String,
        colunaId: String,
        tarefa: Tarefa,
        isChecked: Boolean
    ) {
        if (isChecked && tarefa.status == Status.CONCLUIDA) return
        if (!isChecked && tarefa.status != Status.CONCLUIDA) return

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(error = null) }

                val novoStatus = if (isChecked) Status.CONCLUIDA else Status.INICIADA
                val tarefaParaSalvar = tarefa.copy(status = novoStatus)

                val tarefaAtualizada = tarefaRepository.updateTarefa(quadroId, colunaId, tarefaParaSalvar)

                _uiState.update { currentState ->
                    val colunasAtualizadas = currentState.colunas.map { coluna ->
                        if (coluna.id == colunaId) {
                            coluna.copy(
                                tarefas = coluna.tarefas.map { tarefaExistente ->
                                    if (tarefaExistente.id == tarefa.id) tarefaAtualizada else tarefaExistente
                                }
                            )
                        } else {
                            coluna
                        }
                    }

                    currentState.copy(
                        colunas = colunasAtualizadas,
                        quadro = currentState.quadro?.copy(colunas = colunasAtualizadas)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Erro ao atualizar status da tarefa.")
                }
            }
        }
    }
}