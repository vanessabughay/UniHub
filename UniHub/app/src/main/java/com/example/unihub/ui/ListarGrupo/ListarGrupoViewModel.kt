package com.example.unihub.ui.ListarGrupo

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Grupo
import com.example.unihub.data.repository.GrupoRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class ListarGrupoViewModel(
    private val repository: GrupoRepository
) : ViewModel() {

    private val _grupos = MutableStateFlow<List<Grupo>>(emptyList())
    val grupos: StateFlow<List<Grupo>> = _grupos.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadGrupo()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadGrupo() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getGrupo()
                .map { gruposDoRepositorio ->
                    gruposDoRepositorio.map { grupo ->
                        Grupo(
                            id = grupo.id,
                            nome = grupo.nome,
                            membros = grupo.membros

                        )
                    }.sortedBy { it.nome.lowercase() }
                }
                .catch { exception ->
                    _errorMessage.value = "Falha ao carregar Grupos: ${exception.message}"
                    _grupos.value = emptyList()
                    _isLoading.value = false
                }
                .collect { gruposOrdenadosParaUi ->
                    _grupos.value = gruposOrdenadosParaUi
                    _isLoading.value = false
                }
        }
    }

    /**
     * Exclui um Grupo pelo ID.
     * @param GrupoId O ID do Grupo a ser excluído.
     * @param onResult Callback que informa o resultado da operação (true para sucesso, false para falha).
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun deleteGrupo(grupoId: String, onResult: (sucesso: Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true // Mostrar indicador de carregamento durante a exclusão
            _errorMessage.value = null // Limpar mensagens de erro antigas

            try {
                val id = grupoId.toLongOrNull()
                if (id == null) {
                    _errorMessage.value = "ID de Grupo inválido."
                    _isLoading.value = false
                    onResult(false)
                    return@launch
                }

                val deleteSuccess = repository.deleteGrupo(id.toString())

                if (deleteSuccess) {
                    // Após a exclusão bem-sucedida, recarregue a lista de Grupos.
                    // loadGrupo() já define _isLoading.value = false no seu final.
                    loadGrupo()
                    onResult(true)
                } else {
                    _errorMessage.value = "Falha ao excluir o Grupo no servidor/banco de dados."
                    _isLoading.value = false
                    onResult(false)
                }
            } catch (e: Exception) {
                // Tratar exceções de rede, banco de dados, etc.
                _errorMessage.value = "Erro ao excluir Grupo: ${e.message}"
                _isLoading.value = false
                onResult(false)
            }

        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}