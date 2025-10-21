package com.example.unihub.ui.ListarQuadros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.model.Quadro
import com.example.unihub.data.repository.GrupoRepository
import com.example.unihub.data.repository.QuadroRepository
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val quadroRepository: QuadroRepository,
    private val grupoRepository: GrupoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListarQuadrosUiState())
    val uiState: StateFlow<ListarQuadrosUiState> = _uiState.asStateFlow()

    private var allQuadros: List<Quadro> = emptyList()

    fun carregarQuadros() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val quadrosCarregados = quadroRepository.getQuadros()
                val quadrosDoUsuario = filtrarQuadrosPorAcesso(quadrosCarregados)
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


    private suspend fun filtrarQuadrosPorAcesso(quadros: List<Quadro>): List<Quadro> {
        val usuarioId = TokenManager.usuarioId ?: return quadros
        val acessoPorGrupo = mutableMapOf<Long, Boolean>()

        return quadros.filter { quadro ->
            when {
                quadro.donoId == usuarioId -> true
                quadro.contatoId == usuarioId -> true
                else -> {
                    val grupoId = quadro.grupoId ?: return@filter false
                    val temAcesso = acessoPorGrupo[grupoId] ?: run {
                        val grupo = grupoRepository.fetchGrupoById(grupoId)
                        val possuiAcesso = grupo?.membros.orEmpty().any { membro ->
                            val idsAssociados = listOfNotNull(membro.idContato, membro.ownerId, membro.id)
                            usuarioId in idsAssociados
                        }
                        acessoPorGrupo[grupoId] = possuiAcesso
                        possuiAcesso
                    }
                    temAcesso
                }
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