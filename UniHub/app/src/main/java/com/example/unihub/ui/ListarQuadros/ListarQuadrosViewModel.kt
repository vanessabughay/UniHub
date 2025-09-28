package com.example.unihub.ui.ListarQuadros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.repository.QuadroRepository
import com.example.unihub.data.model.Quadro
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.unihub.data.config.TokenManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ListarQuadrosUiState(
    val isLoading: Boolean = false,
    val quadros: List<Quadro> = emptyList(),
    val searchQuery: String = "", // MUDANÇA: Adicionado o termo de busca
    val error: String? = null
)

class ListarQuadrosViewModel(
    private val repository: QuadroRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListarQuadrosUiState())
    val uiState: StateFlow<ListarQuadrosUiState> = _uiState.asStateFlow()

    private var allQuadros: List<Quadro> = emptyList()

    fun carregarQuadros() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val quadrosCarregados = repository.getQuadros()
                val usuarioId = TokenManager.usuarioId
                val quadrosDoUsuario = if (usuarioId != null &&
                    quadrosCarregados.any { it.donoId != null }) {
                    quadrosCarregados.filter { it.donoId == usuarioId }
                } else {
                    // Quando o backend já filtra por autenticação (ou não envia o campo donoId),
                    // mantemos a lista retornada para garantir que os quadros do usuário sejam exibidos.
                    quadrosCarregados
                }
                allQuadros = quadrosDoUsuario // Salva a lista completa
                _uiState.update {
                    it.copy(
                        quadros = quadrosDoUsuario,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Erro ao carregar quadros.") }
            }
        }
    }


    fun onSearchQueryChanged(query: String) {
        _uiState.update { currentState ->
            val filteredList = if (query.isBlank()) {
                allQuadros // Se a busca estiver vazia, retorna a lista completa
            } else {
                allQuadros.filter {
                    it.nome.contains(query, ignoreCase = true)
                }
            }
            currentState.copy(
                searchQuery = query,
                quadros = filteredList
            )
        }
    }
}