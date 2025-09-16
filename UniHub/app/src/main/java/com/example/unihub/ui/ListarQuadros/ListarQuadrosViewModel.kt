package com.example.unihub.ui.ListarQuadros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.repository.QuadroRepository
import com.example.unihub.data.model.QuadroDePlanejamento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ListarQuadrosUiState(
    val isLoading: Boolean = false,
    val quadros: List<QuadroDePlanejamento> = emptyList(),
    val error: String? = null
)

class ListarQuadrosViewModel(
    private val repository: QuadroRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListarQuadrosUiState())
    val uiState: StateFlow<ListarQuadrosUiState> = _uiState.asStateFlow()

    fun carregarQuadros() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val quadrosCarregados = repository.getQuadros()
                _uiState.update {
                    it.copy(
                        quadros = quadrosCarregados,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Erro ao carregar quadros.") }
            }
        }
    }
}