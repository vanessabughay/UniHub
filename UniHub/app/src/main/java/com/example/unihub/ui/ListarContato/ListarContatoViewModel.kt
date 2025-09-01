package com.example.unihub.ui.ListarContato // Certifique-se que o package está correto

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.repository.ContatoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map

// Data class para o estado da UI, representa um item na lista de contatos
data class ContatoResumoUi(
    val id: Long,
    val nome: String,
    val email: String
)

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class ListarContatoViewModel(
    private val repository: ContatoRepository
) : ViewModel() {

    // StateFlow para a lista de contatos a ser exibida na UI
    private val _contatos = MutableStateFlow<List<ContatoResumoUi>>(emptyList())
    val contatos: StateFlow<List<ContatoResumoUi>> = _contatos.asStateFlow()

    // StateFlow para controlar a visibilidade do indicador de carregamento
    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // StateFlow para mensagens de erro a serem exibidas na UI
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Bloco de inicialização: Carrega os contatos assim que o ViewModel é criado.
     */
    init {
        loadContatos()
    }

    /**
     * Carrega a lista de resumos de contatos do repositório e atualiza os StateFlows.
     * Trata possíveis erros durante a busca de dados.
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadContatos() {
        viewModelScope.launch {
            _isLoading.value = true // Indica que o carregamento começou
            _errorMessage.value = null // Limpa qualquer mensagem de erro anterior

            repository.getContatoResumo() // Retorna Flow<List<AlgumTipoDeContatoDoRepositorio>>
                .map { contatosDoRepositorio ->
                    // Mapeia para ContatoResumoUi E ORDENA AQUI
                    contatosDoRepositorio.map { contato ->
                        ContatoResumoUi(
                            id = contato.id,
                            nome = contato.nome, // Assumindo que contato.nome não é nulo
                            email = contato.email // Assumindo que contato.email não é nulo
                        )
                    }.sortedBy { it.nome.lowercase() } // <<<<<<<<<<<<<<<<<<<< ORDENAÇÃO ADICIONADA
                }
                .catch { exception ->
                    _errorMessage.value = "Falha ao carregar contatos: ${exception.message}"
                    _contatos.value = emptyList() // Opcional: limpar a lista em caso de erro
                    _isLoading.value = false
                }
                .collect { contatosOrdenadosParaUi ->
                    _contatos.value = contatosOrdenadosParaUi // Atualiza a UI com a lista já mapeada e ordenada
                    _isLoading.value = false
                }
        }
    }

    /**
     * Limpa a mensagem de erro. Deve ser chamado pela UI após a mensagem ser exibida.
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
