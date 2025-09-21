package com.example.unihub.ui.ListarAvaliacao

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Avaliacao
import com.example.unihub.data.repository.AvaliacaoRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class ListarAvaliacaoViewModel(
    private val repository: AvaliacaoRepository
) : ViewModel() {

    private val _avaliacoes = MutableStateFlow<List<Avaliacao>>(emptyList())
    val avaliacoes: StateFlow<List<Avaliacao>> = _avaliacoes.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadAvaliacao()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadAvaliacao() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getAvaliacao()
                .map { avaliacoesDoRepositorio ->
                    avaliacoesDoRepositorio.map { avaliacao ->
                        Avaliacao(
                            id = avaliacao.id,

                         descricao= avaliacao.descricao,
                         disciplina = avaliacao.disciplina,
                         tipoAvaliacao = avaliacao.tipoAvaliacao,
                         dataEntrega = avaliacao.dataEntrega,
                             nota = avaliacao.nota,
                         peso = avaliacao.peso,
                         integrantes = avaliacao.integrantes,
                         prioridade = avaliacao.prioridade,
                             estado= avaliacao.estado,
                             dificuldade= avaliacao.dificuldade,
                        )
                    }.sortedBy { it.descricao?.lowercase() }
                }
                .catch { exception ->
                    val detalheErro = if (exception.message.isNullOrBlank()) {
                        "Causa desconhecida. Verifique os logs." // Ou uma mensagem mais genérica
                    } else {
                        exception.message
                    }
                    _errorMessage.value = "Falha ao carregar Avaliações: $detalheErro"
                    // Adicionar log para depuração
                    Log.e("ListarAvaliacaoViewModel", "Erro em loadAvaliacao: ", exception)
                    _avaliacoes.value = emptyList()
                    _isLoading.value = false
                }
                .collect { avalioacoesOrdenadosParaUi ->
                    _avaliacoes.value = avalioacoesOrdenadosParaUi
                    _isLoading.value = false
                }
        }
    }

    /**
     * Exclui Avaliacao pelo ID.
     * @param AvaliacaoId O ID do Avaliacao a ser excluído.
     * @param onResult Callback que informa o resultado da operação (true para sucesso, false para falha).
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun deleteAvaliacao(avaliacaoId: String, onResult: (sucesso: Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true // Mostrar indicador de carregamento durante a exclusão
            _errorMessage.value = null // Limpar mensagens de erro antigas

            try {
                val id = avaliacaoId.toLongOrNull()
                if (id == null) {
                    _errorMessage.value = "ID de Avaliação inválido."
                    _isLoading.value = false
                    onResult(false)
                    return@launch
                }

                val deleteSuccess = repository.deleteAvaliacao(id.toString())

                if (deleteSuccess) {
                    // Após a exclusão bem-sucedida, recarregue a lista de avaliacoes.

                    loadAvaliacao()
                    onResult(true)
                } else {
                    _errorMessage.value = "Falha ao excluir o Avaliação no servidor/banco de dados."
                    _isLoading.value = false
                    onResult(false)
                }
            } catch (e: Exception) {
                // Tratar exceções de rede, banco de dados, etc.
                _errorMessage.value = "Erro ao excluir Avalição: ${e.message}"
                _isLoading.value = false
                onResult(false)
            }

        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}