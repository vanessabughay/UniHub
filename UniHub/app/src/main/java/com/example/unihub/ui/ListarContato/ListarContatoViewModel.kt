package com.example.unihub.ui.ListarContato

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.repository.ContatoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class ContatoResumoUi(
    val id: Long,
    val nome: String,
    val email: String
)

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class ListarContatoViewModel(
    private val repository: ContatoRepository
) : ViewModel() {

    private val _contatos = MutableStateFlow<List<ContatoResumoUi>>(emptyList())
    val contatos: StateFlow<List<ContatoResumoUi>> = _contatos.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadContatos()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadContatos() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getContatoResumo()
                .map { contatosDoRepositorio ->
                    contatosDoRepositorio.map { contato ->
                        ContatoResumoUi(
                            id = contato.id,
                            nome = contato.nome,
                            email = contato.email
                        )
                    }.sortedBy { it.nome.lowercase() }
                }
                .catch { exception ->
                    _errorMessage.value = "Falha ao carregar contatos: ${exception.message}"
                    _contatos.value = emptyList()
                    _isLoading.value = false
                }
                .collect { contatosOrdenadosParaUi ->
                    _contatos.value = contatosOrdenadosParaUi
                    _isLoading.value = false
                }
        }
    }

    /**
     * Exclui um contato pelo ID.
     * @param contatoId O ID do contato a ser excluído.
     * @param onResult Callback que informa o resultado da operação (true para sucesso, false para falha).
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun deleteContato(contatoId: String, onResult: (sucesso: Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true // Mostrar indicador de carregamento durante a exclusão
            _errorMessage.value = null // Limpar mensagens de erro antigas

            try {
                val id = contatoId.toLongOrNull()
                if (id == null) {
                    _errorMessage.value = "ID de contato inválido."
                    _isLoading.value = false
                    onResult(false)
                    return@launch
                }

                val deleteSuccess = repository.deleteContato(id.toString())

                if (deleteSuccess) {
                    // Após a exclusão bem-sucedida, recarregue a lista de contatos.
                    // loadContatos() já define _isLoading.value = false no seu final.
                    loadContatos()
                    onResult(true)
                } else {
                    _errorMessage.value = "Falha ao excluir o contato no servidor/banco de dados."
                    _isLoading.value = false
                    onResult(false)
                }
            } catch (e: Exception) {
                // Tratar exceções de rede, banco de dados, etc.
                _errorMessage.value = "Erro ao excluir contato: ${e.message}"
                _isLoading.value = false
                onResult(false)
            }

        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}

