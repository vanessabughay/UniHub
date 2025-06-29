package com.example.unihub.ui.VisualizarDisciplina

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.remote.RetrofitClient // Para instanciar o repositório
import com.example.unihub.data.repository.DisciplinaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Define o estado da UI para a tela de visualização
data class VisualizarDisciplinaUiState(
    val disciplina: Disciplina? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class VisualizarDisciplinaViewModel(
    private val repository: DisciplinaRepository = DisciplinaRepository(RetrofitClient.disciplinaService)
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisualizarDisciplinaUiState())
    val uiState: StateFlow<VisualizarDisciplinaUiState> = _uiState.asStateFlow()

    // Função para carregar a disciplina pelo ID
    fun loadDisciplina(disciplinaId: String?) {
        if (disciplinaId == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "ID da disciplina não fornecido.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                repository.getDisciplinaById(disciplinaId).collect { disciplina ->
                    _uiState.value = _uiState.value.copy(disciplina = disciplina, isLoading = false)
                    if (disciplina == null) {
                        _uiState.value = _uiState.value.copy(errorMessage = "Disciplina não encontrada.")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar disciplina: ${e.message}"
                )
            }
        }
    }

    // Função para limpar mensagens de erro se necessário
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}