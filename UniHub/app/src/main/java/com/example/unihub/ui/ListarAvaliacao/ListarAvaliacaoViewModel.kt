package com.example.unihub.ui.ListarAvaliacao

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.dto.AvaliacaoRequestDto
import com.example.unihub.data.dto.ContatoIdDto
import com.example.unihub.data.dto.DisciplinaIdDto
import com.example.unihub.data.model.Avaliacao
import com.example.unihub.data.model.EstadoAvaliacao
import com.example.unihub.data.model.Modalidade
import com.example.unihub.data.model.Prioridade
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

    private val _isLoading = MutableStateFlow(false)
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
                .map { lista ->
                    // Aqui apenas garantimos ordenação. Preservamos todos os campos do model vindo do repo.
                    lista.sortedBy { it.descricao?.lowercase() }
                }
                .catch { exception ->
                    val detalheErro = exception.message ?: "Causa desconhecida. Verifique os logs."
                    _errorMessage.value = "Falha ao carregar Avaliações: $detalheErro"
                    Log.e("ListarAvaliacaoViewModel", "Erro em loadAvaliacao: ", exception)
                    _avaliacoes.value = emptyList()
                    _isLoading.value = false
                }
                .collect { ordenadas ->
                    _avaliacoes.value = ordenadas
                    _isLoading.value = false
                }
        }
    }

    /**
     * Exclui Avaliacao pelo ID.
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun deleteAvaliacao(avaliacaoId: String, onResult: (sucesso: Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
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
                    loadAvaliacao()
                    onResult(true)
                } else {
                    _errorMessage.value = "Falha ao excluir a avaliação no servidor."
                    _isLoading.value = false
                    onResult(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao excluir avaliação: ${e.message}"
                _isLoading.value = false
                onResult(false)
            }
        }
    }

    /**
     * Concluir/Reativar avaliação (altera estado) com confirmação na UI.
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun toggleConcluida(av: Avaliacao, marcada: Boolean, onResult: (Boolean) -> Unit) {
        val id = av.id ?: return onResult(false)

        // Fallbacks seguros (caso algum campo venha nulo do backend)
        val modalidade = av.modalidade ?: Modalidade.INDIVIDUAL
        val prioridade = av.prioridade ?: Prioridade.MEDIA
        val receberNotificacoes = av.receberNotificacoes != false

        val disciplinaDto = av.disciplina?.id?.let { DisciplinaIdDto(it) }
        val integrantesDto = av.integrantes.orEmpty()
            .mapNotNull { it.id }
            .map { ContatoIdDto(it) }

        val novoEstado = if (marcada) EstadoAvaliacao.CONCLUIDA else EstadoAvaliacao.A_REALIZAR

        val req = AvaliacaoRequestDto(
            id = id,
            descricao = av.descricao,
            disciplina = disciplinaDto,
            tipoAvaliacao = av.tipoAvaliacao,
            modalidade = modalidade,
            dataEntrega = av.dataEntrega, // backend aceita dd-mm ou datetime ISO que você já usa
            nota = av.nota,
            peso = av.peso,
            integrantes = integrantesDto,
            prioridade = prioridade,
            estado = novoEstado,
            dificuldade = av.dificuldade,
            receberNotificacoes = receberNotificacoes
        )

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val ok = repository.updateAvaliacao(id, req)
                if (ok) {
                    loadAvaliacao()
                    onResult(true)
                } else {
                    _errorMessage.value = "Não foi possível atualizar o estado."
                    _isLoading.value = false
                    onResult(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao atualizar: ${e.message}"
                _isLoading.value = false
                onResult(false)
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun updateNota(av: Avaliacao, novaNota: Double?, onResult: (Boolean) -> Unit) {
        val id = av.id ?: return onResult(false)

        // Monta o mesmo DTO do toggleConcluida, só trocando a 'nota'
        val req = AvaliacaoRequestDto(
            id = id,
            descricao = av.descricao,
            disciplina = av.disciplina?.id?.let { DisciplinaIdDto(it) },
            tipoAvaliacao = av.tipoAvaliacao,
            modalidade = av.modalidade ?: Modalidade.INDIVIDUAL,
            dataEntrega = av.dataEntrega, // backend usa LocalDate.parse -> "yyyy-MM-dd"
            nota = novaNota,
            peso = av.peso,
            integrantes = av.integrantes.orEmpty().mapNotNull { it.id }.map { ContatoIdDto(it) },
            prioridade = av.prioridade ?: Prioridade.MEDIA,
            estado = av.estado ?: EstadoAvaliacao.A_REALIZAR,
            dificuldade = av.dificuldade,
            receberNotificacoes = av.receberNotificacoes != false
        )

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val ok = repository.updateAvaliacao(id, req)
                if (ok) {
                    loadAvaliacao()
                    onResult(true)
                } else {
                    _errorMessage.value = "Não foi possível salvar a nota."
                    _isLoading.value = false
                    onResult(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao salvar nota: ${e.message}"
                _isLoading.value = false
                onResult(false)
            }
        }
    }


    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
