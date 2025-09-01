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

            repository.getContatoResumo() // Chama a função do repositório que retorna um Flow
                .catch { exception ->
                    // Em caso de erro na coleta do Flow (ex: IOException, HttpException)
                    _errorMessage.value = "Falha ao carregar contatos: ${exception.message}"
                    _isLoading.value = false // Indica que o carregamento terminou (com erro)
                }
                .collect { contatosDoRepositorio ->
                    // Mapeia a lista de modelos do repositório para a lista de modelos da UI
                    val contatosParaUi = contatosDoRepositorio.map { contato ->
                        ContatoResumoUi(
                            id = contato.id,
                            nome = contato.nome,
                            email = contato.email
                        )
                    }
                    _contatos.value = contatosParaUi // Atualiza a lista de contatos na UI
                    _isLoading.value = false // Indica que o carregamento terminou (com sucesso)
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
