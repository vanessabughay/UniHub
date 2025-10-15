package com.example.unihub.ui.ManterTarefa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Status
import com.example.unihub.data.model.Tarefa
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.GrupoRepository
import com.example.unihub.data.repository.QuadroRepository
import com.example.unihub.data.repository.TarefaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

sealed class TarefaFormResult {
    object Idle : TarefaFormResult()
    object Success : TarefaFormResult()
    data class Error(val message: String) : TarefaFormResult()
}

data class ResponsavelOption(
    val id: Long,
    val nome: String
)

class TarefaFormViewModel(
    private val repository: TarefaRepository,
    private val quadroRepository: QuadroRepository,
    private val grupoRepository: GrupoRepository,
    private val contatoRepository: ContatoRepository
) : ViewModel() {

    private val _tarefaState = MutableStateFlow<Tarefa?>(null)
    val tarefa: StateFlow<Tarefa?> = _tarefaState.asStateFlow()

    private val _formResult = MutableStateFlow<TarefaFormResult>(TarefaFormResult.Idle)
    val formResult: StateFlow<TarefaFormResult> = _formResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _responsaveisDisponiveis = MutableStateFlow<List<ResponsavelOption>>(emptyList())
    val responsaveisDisponiveis: StateFlow<List<ResponsavelOption>> = _responsaveisDisponiveis.asStateFlow()

    private val _responsaveisSelecionados = MutableStateFlow<Set<Long>>(emptySet())
    val responsaveisSelecionados: StateFlow<Set<Long>> = _responsaveisSelecionados.asStateFlow()

    fun carregarTarefa(quadroId: String, colunaId: String, tarefaId: String) {
        // _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val tarefaCarregada = repository.getTarefa(quadroId, colunaId, tarefaId)
                _tarefaState.value = tarefaCarregada
                _responsaveisSelecionados.value = tarefaCarregada.responsaveisIds.toSet()
            } catch (e: Exception) {
                _formResult.value = TarefaFormResult.Error(e.message ?: "Erro ao carregar tarefa.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cadastrarTarefa(quadroId: String, colunaId: String, novaTarefa: Tarefa) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val responsaveisOrdenados = ordenarResponsaveisSelecionados()
                val tarefaComResponsaveis = novaTarefa.copy(responsaveisIds = responsaveisOrdenados)
                repository.createTarefa(quadroId, colunaId, tarefaComResponsaveis)
                _formResult.value = TarefaFormResult.Success
            } catch (e: Exception) {
                _formResult.value = TarefaFormResult.Error(e.message ?: "Erro ao salvar tarefa.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun atualizarTarefa(quadroId: String, colunaId: String, tarefaAtualizada: Tarefa) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val tarefaAtual = _tarefaState.value
                var tarefaParaSalvar = tarefaAtualizada

                tarefaParaSalvar = if (tarefaAtualizada.status == Status.CONCLUIDA && tarefaAtual?.dataFim == null) {
                    tarefaAtualizada.copy(dataFim = System.currentTimeMillis())
                } else if (tarefaAtual?.status == Status.CONCLUIDA && tarefaAtualizada.status != Status.CONCLUIDA) {
                    tarefaAtualizada.copy(dataFim = null)
                } else {
                    tarefaAtualizada
                }

                val responsaveisOrdenados = ordenarResponsaveisSelecionados()
                tarefaParaSalvar = tarefaParaSalvar.copy(responsaveisIds = responsaveisOrdenados)

                val tarefaSalva = repository.updateTarefa(quadroId, colunaId, tarefaParaSalvar)
                _tarefaState.value = tarefaSalva
                _formResult.value = TarefaFormResult.Success
            } catch (e: Exception) {
                _formResult.value = TarefaFormResult.Error(e.message ?: "Erro ao atualizar tarefa.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun excluirTarefa(quadroId: String, colunaId: String, tarefaId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteTarefa(quadroId, colunaId, tarefaId)
                _tarefaState.value = null
                _formResult.value = TarefaFormResult.Success
            } catch (e: Exception) {
                _formResult.value = TarefaFormResult.Error(e.message ?: "Erro ao excluir tarefa.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun carregarResponsaveis(quadroId: String) {
        viewModelScope.launch {
            try {
                val quadro = quadroRepository.getQuadroById(quadroId)
                val opcoes = mutableListOf<ResponsavelOption>()

                quadro?.contatoId?.let { contatoId ->
                    contatoRepository.fetchContatoById(contatoId)?.let { contato ->
                        val id = contato.id
                        val nome = contato.nome
                        if (id != null && !nome.isNullOrBlank()) {
                            opcoes.add(ResponsavelOption(id, nome))
                        }
                    }
                }

                quadro?.grupoId?.let { grupoId ->
                    grupoRepository.fetchGrupoById(grupoId)?.membros?.forEach { membro ->
                        val id = membro.id
                        val nome = membro.nome
                        if (id != null && !nome.isNullOrBlank()) {
                            opcoes.add(ResponsavelOption(id, nome))
                        }
                    }
                }

                val unicosOrdenados = opcoes
                    .distinctBy { it.id }
                    .sortedBy { it.nome.lowercase(Locale.getDefault()) }

                _responsaveisDisponiveis.value = unicosOrdenados
                _responsaveisSelecionados.update { selecionados ->
                    selecionados.filter { id -> unicosOrdenados.any { it.id == id } }.toSet()
                }
            } catch (e: Exception) {
                _responsaveisDisponiveis.value = emptyList()
            }
        }
    }

    fun atualizarResponsaveisSelecionados(ids: Set<Long>) {
        _responsaveisSelecionados.value = ids
    }

    fun resetFormResult() {
        _formResult.value = TarefaFormResult.Idle
    }

    private fun ordenarResponsaveisSelecionados(): List<Long> {
        val selecionados = _responsaveisSelecionados.value
        if (selecionados.isEmpty()) return emptyList()

        val disponiveisOrdenados = _responsaveisDisponiveis.value
        val ordenadosPorNome = disponiveisOrdenados
            .filter { selecionados.contains(it.id) }
            .map { it.id }

        val naoListados = selecionados.filter { id -> ordenadosPorNome.contains(id).not() }

        return ordenadosPorNome + naoListados
    }
}
