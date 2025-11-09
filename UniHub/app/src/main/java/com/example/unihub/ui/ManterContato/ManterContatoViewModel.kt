package com.example.unihub.ui.ManterContato



import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Contato // Seu modelo de dados Contato
import com.example.unihub.data.repository.ContatoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map


// Estado da UI para a tela ManterContato
data class ManterContatoUiState(
    val nome: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val erro: String? = null,
    val sucesso: Boolean = false,
    val isExclusao: Boolean = false // Para controlar o fluxo após exclusão
)

class ManterContatoViewModel(
    private val repository: ContatoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManterContatoUiState())
    val uiState: StateFlow<ManterContatoUiState> = _uiState.asStateFlow()

    // Para carregar os dados de um contato existente para edição
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadContato(id: String) {
        val longId = id.toLongOrNull()
        if (longId != null) {
            _uiState.value = _uiState.value.copy(isLoading = true, erro = null)
            viewModelScope.launch {
                try {
                    repository.getContatoById(longId).collect { contato ->
                        if (contato != null) {
                            _uiState.value = _uiState.value.copy(
                                nome = contato.nome ?: "",
                                email = contato.email ?: "",
                                isLoading = false
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                erro = "Contato não encontrado",
                                isLoading = false
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(erro = e.message, isLoading = false)
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(erro = "ID de contato inválido")
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun createContato(nome: String, email: String) {
        if (nome.isBlank() || email.isBlank()) {
            _uiState.value = _uiState.value.copy(erro = "Nome e E-mail são obrigatórios.")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, erro = null, isExclusao = false)
        viewModelScope.launch {
            try {
                // Criar o objeto Contato do modelo de dados
                val novoContato = Contato(id = null, nome = nome, email = email)
                repository.addContato(novoContato) // addContato agora não retorna nada significativo
                _uiState.value = _uiState.value.copy(sucesso = true, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(erro = e.message, isLoading = false, sucesso = false)
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun updateContato(id: String, nome: String, email: String) {
        val longId = id.toLongOrNull()
        if (longId == null) {
            _uiState.value = _uiState.value.copy(erro = "ID de contato inválido para atualização.")
            return
        }
        if (nome.isBlank() || email.isBlank()) {
            _uiState.value = _uiState.value.copy(erro = "Nome e E-mail são obrigatórios.")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, erro = null, isExclusao = false)
        viewModelScope.launch {
            try {
                // Criar o objeto Contato do modelo de dados
                val contatoAtualizado = Contato(id = longId, nome = nome, email = email)
                val result = repository.updateContato(contatoAtualizado) // updateContato retorna Boolean
                _uiState.value = _uiState.value.copy(sucesso = result, isLoading = false)
                if (!result) {
                    _uiState.value = _uiState.value.copy(erro = "Falha ao atualizar o contato.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(erro = e.message, isLoading = false, sucesso = false)
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun deleteContato(id: String) {
        val longId = id.toLongOrNull()
        if (longId == null) {
            _uiState.value = _uiState.value.copy(erro = "ID de contato inválido para exclusão.")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, erro = null, isExclusao = true)
        viewModelScope.launch {
            try {
                val result = repository.deleteContato(longId) // deleteContato retorna Boolean
                _uiState.value = _uiState.value.copy(sucesso = result, isLoading = false)
                if (!result) {
                    _uiState.value = _uiState.value.copy(erro = "Falha ao excluir o contato.")
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
