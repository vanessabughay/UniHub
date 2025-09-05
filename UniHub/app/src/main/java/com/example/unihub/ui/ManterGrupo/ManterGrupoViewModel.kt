package com.example.unihub.ui.ManterGrupo

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Grupo
import com.example.unihub.data.repository.GrupoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ManterGrupoUiState(
    val nome: String = "",
    val isLoading: Boolean = false,
    val erro: String? = null,
    val sucesso: Boolean = false,
    val isExclusao: Boolean = false // Para controlar o fluxo após exclusão
)

class ManterGrupoViewModel(
    private val repository: GrupoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManterGrupoUiState())
    val uiState: StateFlow<com.example.unihub.ui.ManterGrupo.ManterGrupoUiState> = _uiState.asStateFlow()

    // Para carregar os dados de um Grupo existente para edição
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadGrupo(id: String) {
        val longId = id.toLongOrNull()
        if (longId != null) {
            _uiState.value = _uiState.value.copy(isLoading = true, erro = null)
            viewModelScope.launch {
                try {
                    repository.getGrupoById(longId).collect { grupo ->
                        if (grupo != null) {
                            _uiState.value = _uiState.value.copy(
                                nome = grupo.nome ?: "",


                                //email = grupo.email ?: "",

                                isLoading = false
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                erro = "Grupo não encontrado",
                                isLoading = false
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(erro = e.message, isLoading = false)
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(erro = "ID de Grupo inválido")
        }
    }

    fun createGrupo(nome: String) {
        if (nome.isBlank()) {
            _uiState.value = _uiState.value.copy(erro = "Nome é obrigatório.")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, erro = null, isExclusao = false)
        viewModelScope.launch {
            try {
                // Criar o objeto Grupo do modelo de dados
                val novoGrupo = Grupo(nome = nome, membros = emptyList())
                repository.addGrupo(novoGrupo)
                _uiState.value = _uiState.value.copy(sucesso = true, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(erro = e.message, isLoading = false, sucesso = false)
            }
        }
    }

    fun updateGrupo(id: String, nome: String) {
        val longId = id.toLongOrNull()
        if (longId == null) {
            _uiState.value = _uiState.value.copy(erro = "ID de Grupo inválido para atualização.")
            return
        }
        if (nome.isBlank() ) {
            _uiState.value = _uiState.value.copy(erro = "Nome é obrigatório.")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, erro = null, isExclusao = false)
        viewModelScope.launch {
            try {
                // Criar o objeto Grupo do modelo de dados
                val grupoAtualizado = Grupo(id = longId, nome = nome, membros = emptyList())
                val result = repository.updateGrupo(grupoAtualizado) // updateGrupo retorna Boolean
                _uiState.value = _uiState.value.copy(sucesso = result, isLoading = false)
                if (!result) {
                    _uiState.value = _uiState.value.copy(erro = "Falha ao atualizar o Grupo.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(erro = e.message, isLoading = false, sucesso = false)
            }
        }
    }

    fun deleteGrupo(id: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, erro = null, isExclusao = true)
        viewModelScope.launch {
            try {
                val result = repository.deleteGrupo(id) // deleteGrupo retorna Boolean
                _uiState.value = _uiState.value.copy(sucesso = result, isLoading = false)
                if (!result) {
                    _uiState.value = _uiState.value.copy(erro = "Falha ao excluir o Grupo.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(erro = e.message, isLoading = false, sucesso = false)
            }
        }
    }

    // Função para limpar o estado de sucesso/erro após a navegação ou exibição da mensagem
    fun onEventoConsumido() {
        _uiState.value = _uiState.value.copy(sucesso = false, erro = null, isExclusao = false)
    }
}
