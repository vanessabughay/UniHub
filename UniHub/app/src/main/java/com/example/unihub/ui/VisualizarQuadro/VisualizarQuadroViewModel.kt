package com.example.unihub.ui.VisualizarQuadro

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.repository.QuadroRepository
import com.example.unihub.data.model.Coluna
import com.example.unihub.data.model.Quadro
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
    private val quadroRepository: QuadroRepository
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
}