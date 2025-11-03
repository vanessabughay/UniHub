package com.example.unihub.ui.ManterTarefa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.model.Comentario
import com.example.unihub.data.model.Contato
import com.example.unihub.data.model.Status
import com.example.unihub.data.model.Tarefa
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.ContatoResumo
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

sealed class ComentarioActionResult {
    data class Success(
        val message: String,
        val clearNewComment: Boolean = false,
        val resetEditing: Boolean = false
    ) : ComentarioActionResult()

    data class Error(val message: String) : ComentarioActionResult()
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

    private val _comentarios = MutableStateFlow<List<Comentario>>(emptyList())
    val comentarios: StateFlow<List<Comentario>> = _comentarios.asStateFlow()

    private val _comentariosCarregando = MutableStateFlow(false)
    val comentariosCarregando: StateFlow<Boolean> = _comentariosCarregando.asStateFlow()

    private val _receberNotificacoes = MutableStateFlow(true)
    val receberNotificacoes: StateFlow<Boolean> = _receberNotificacoes.asStateFlow()

    private val _comentarioResultado = MutableStateFlow<ComentarioActionResult?>(null)
    val comentarioResultado: StateFlow<ComentarioActionResult?> = _comentarioResultado.asStateFlow()

    fun carregarTarefa(quadroId: String, colunaId: String, tarefaId: String) {
        // _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val tarefaCarregada = repository.getTarefa(quadroId, colunaId, tarefaId)
                _tarefaState.value = tarefaCarregada
                val idsResponsaveis = tarefaCarregada.responsaveisIds
                _responsaveisSelecionados.value = idsResponsaveis.toSet()
                if (idsResponsaveis.isNotEmpty()) {
                    carregarResponsaveis(quadroId, idsResponsaveis)
                }
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

                val responsaveisOrdenados = ordenarResponsaveisSelecionados()
                val tarefaParaSalvar = tarefaAtualizada.copy(responsaveisIds = responsaveisOrdenados)

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

    fun carregarComentarios(quadroId: String, colunaId: String, tarefaId: String) {
        viewModelScope.launch {
            _comentariosCarregando.value = true
            try {
                val response = repository.carregarComentarios(quadroId, colunaId, tarefaId)
                _comentarios.value = response.comentarios
                _receberNotificacoes.value = response.receberNotificacoes
            } catch (e: Exception) {
                _comentarioResultado.value = ComentarioActionResult.Error(
                    e.message ?: "Erro ao carregar comentários."
                )
            } finally {
                _comentariosCarregando.value = false
            }
        }
    }

    fun criarComentario(quadroId: String, colunaId: String, tarefaId: String, conteudo: String) {
        if (conteudo.isBlank()) {
            _comentarioResultado.value = ComentarioActionResult.Error("O comentário não pode ser vazio.")
            return
        }
        viewModelScope.launch {
            try {
                val novoComentario = repository.criarComentario(quadroId, colunaId, tarefaId, conteudo.trim())
                _comentarios.update { listaAtual -> listOf(novoComentario) + listaAtual }
                _comentarioResultado.value = ComentarioActionResult.Success(
                    message = "Comentário adicionado com sucesso!",
                    clearNewComment = true,
                    resetEditing = true
                )
            } catch (e: Exception) {
                _comentarioResultado.value = ComentarioActionResult.Error(
                    e.message ?: "Erro ao salvar comentário."
                )
            }
        }
    }

    fun atualizarComentario(
        quadroId: String,
        colunaId: String,
        tarefaId: String,
        comentarioId: String,
        conteudo: String
    ) {
        if (conteudo.isBlank()) {
            _comentarioResultado.value = ComentarioActionResult.Error("O comentário não pode ser vazio.")
            return
        }
        viewModelScope.launch {
            try {
                val comentarioAtualizado = repository.atualizarComentario(
                    quadroId,
                    colunaId,
                    tarefaId,
                    comentarioId,
                    conteudo.trim()
                )
                _comentarios.update { lista ->
                    lista.map { comentario ->
                        if (comentario.id == comentarioId) comentarioAtualizado else comentario
                    }
                }
                _comentarioResultado.value = ComentarioActionResult.Success(
                    message = "Comentário atualizado com sucesso!",
                    resetEditing = true
                )
            } catch (e: Exception) {
                _comentarioResultado.value = ComentarioActionResult.Error(
                    e.message ?: "Erro ao atualizar comentário."
                )
            }
        }
    }

    fun excluirComentario(quadroId: String, colunaId: String, tarefaId: String, comentarioId: String) {
        viewModelScope.launch {
            try {
                repository.excluirComentario(quadroId, colunaId, tarefaId, comentarioId)
                _comentarios.update { lista -> lista.filterNot { it.id == comentarioId } }
                _comentarioResultado.value = ComentarioActionResult.Success(
                    message = "Comentário excluído com sucesso!",
                    resetEditing = true
                )
            } catch (e: Exception) {
                _comentarioResultado.value = ComentarioActionResult.Error(
                    e.message ?: "Erro ao excluir comentário."
                )
            }
        }
    }

    fun atualizarPreferenciaTarefa(
        quadroId: String,
        colunaId: String,
        tarefaId: String,
        receberNotificacoes: Boolean
    ) {
        viewModelScope.launch {
            try {
                val resposta = repository.atualizarPreferenciaTarefa(
                    quadroId,
                    colunaId,
                    tarefaId,
                    receberNotificacoes
                )
                _receberNotificacoes.value = resposta.receberNotificacoes
                _comentarioResultado.value = ComentarioActionResult.Success(
                    message = if (resposta.receberNotificacoes) {
                        "Notificações habilitadas"
                    } else {
                        "Notificações desabilitadas"
                    }
                )
            } catch (e: Exception) {
                _comentarioResultado.value = ComentarioActionResult.Error(
                    e.message ?: "Erro ao atualizar preferências."
                )
            }
        }
    }

    fun definirReceberNotificacoesLocal(receber: Boolean) {
        _receberNotificacoes.value = receber
    }

    fun resetComentarioResultado() {
        _comentarioResultado.value = null
    }

    fun carregarResponsaveis(quadroId: String, responsavelIds: List<Long> = emptyList()) {
        viewModelScope.launch {
            try {
                val usuarioLogadoId = TokenManager.usuarioId
                val nomeUsuarioLogado = TokenManager.nomeUsuario?.trimmedOrNull()
                val emailUsuarioLogadoNormalizado = TokenManager.emailUsuario
                    ?.trimmedOrNull()
                    ?.lowercase()

                val contatosResumo = runCatching { contatoRepository.fetchContatosResumo() }
                    .getOrElse { emptyList() }

                val contatosDoUsuario = if (usuarioLogadoId != null) {
                    contatosResumo.filter { it.ownerId == usuarioLogadoId }
                } else {
                    emptyList()
                }

                val contatosPorEmail = contatosDoUsuario
                    .mapNotNull { resumo ->
                        resumo.email.trimmedOrNull()?.lowercase()?.let { it to resumo }
                    }
                    .toMap()

                val contatosPorIdContato = contatosDoUsuario.associateBy { it.id }

                val quadro = quadroRepository.getQuadroById(quadroId)
                val grupo = quadro?.grupoId?.let { grupoId ->
                    runCatching { grupoRepository.fetchGrupoById(grupoId) }.getOrNull()
                }
                val membrosOrdenados = grupo?.membros
                    .orEmpty()
                    .sortedBy { contato -> contato.id ?: Long.MAX_VALUE }

                val opcoes = mutableListOf<ResponsavelOption>()
                val idsSemDados = responsavelIds.toMutableSet()
                val membrosExibidos = linkedSetOf<String>()
                var indiceSequencial = 0

                fun adicionarResponsavel(
                    contato: Contato?,
                    fallbackId: Long? = null,
                    fallbackNome: String? = null,
                    fallbackEmail: String? = null
                ) {
                    val resultado = construirResponsavelOption(
                        contato = contato,
                        index = indiceSequencial++,
                        fallbackId = fallbackId,
                        fallbackNome = fallbackNome,
                        fallbackEmail = fallbackEmail,
                        usuarioLogadoId = usuarioLogadoId,
                        nomeUsuarioLogado = nomeUsuarioLogado,
                        emailUsuarioLogadoNormalizado = emailUsuarioLogadoNormalizado,
                        contatosPorIdContato = contatosPorIdContato,
                        contatosPorEmail = contatosPorEmail
                    ) ?: return

                    val (chave, option) = resultado
                    if (membrosExibidos.add(chave)) {
                        opcoes.add(option)
                        idsSemDados.remove(option.id)
                    }
                }

                val donoId = quadro?.donoId ?: usuarioLogadoId
                if (donoId != null) {
                    val contatoDoDono = runCatching { contatoRepository.fetchContatoById(donoId) }
                        .getOrNull()
                    adicionarResponsavel(
                        contato = contatoDoDono,
                        fallbackId = donoId,
                        fallbackNome = if (donoId == usuarioLogadoId) nomeUsuarioLogado else null
                    )
                }

                quadro?.contatoId?.let { contatoId ->
                    val responsavelContato = runCatching { contatoRepository.fetchContatoById(contatoId) }
                        .getOrNull()
                    adicionarResponsavel(
                        contato = responsavelContato,
                        fallbackId = contatoId,
                        fallbackEmail = responsavelContato?.email
                    )
                }

                membrosOrdenados.forEach { membro ->
                    adicionarResponsavel(contato = membro)
                }

                responsavelIds.forEach { responsavelId ->
                    if (opcoes.none { it.id == responsavelId }) {
                        val responsavelExtra = runCatching { contatoRepository.fetchContatoById(responsavelId) }
                            .getOrNull()
                        adicionarResponsavel(
                            contato = responsavelExtra,
                            fallbackId = responsavelId,
                            fallbackEmail = responsavelExtra?.email
                        )
                    }
                }

                if (idsSemDados.isNotEmpty()) {
                    idsSemDados.forEach { id ->
                        opcoes.add(ResponsavelOption(id = id, nome = "Responsável #$id"))
                    }
                }

                val unicosOrdenados = opcoes
                    .distinctBy { it.id }
                    .sortedBy { it.nome.lowercase(Locale.getDefault()) }

                _responsaveisDisponiveis.value = unicosOrdenados
                val idsSelecionados = if (responsavelIds.isNotEmpty()) {
                    responsavelIds.toSet()
                } else {
                    _responsaveisSelecionados.value
                }
                _responsaveisSelecionados.value = idsSelecionados
                    .filter { id -> unicosOrdenados.any { it.id == id } }
                    .toSet()
            } catch (e: Exception) {
                _responsaveisDisponiveis.value = emptyList()
            }
        }
    }



    private fun construirResponsavelOption(
        contato: Contato?,
        index: Int,
        fallbackId: Long?,
        fallbackNome: String?,
        fallbackEmail: String?,
        usuarioLogadoId: Long?,
        nomeUsuarioLogado: String?,
        emailUsuarioLogadoNormalizado: String?,
        contatosPorIdContato: Map<Long, ContatoResumo>,
        contatosPorEmail: Map<String, ContatoResumo>
    ): Pair<String, ResponsavelOption>? {
        val emailDisponivel = contato?.email.trimmedOrNull() ?: fallbackEmail.trimmedOrNull()
        val emailNormalizado = emailDisponivel?.lowercase()
        val nomeDisponivel = contato?.nome.trimmedOrNull() ?: fallbackNome.trimmedOrNull()
        val idResponsavel = contato?.idContato ?: contato?.ownerId ?: contato?.id ?: fallbackId

        val chaveUnica = when {
            contato?.idContato != null -> "idContato:${contato.idContato}"
            emailNormalizado != null -> "email:$emailNormalizado"
            contato?.id != null -> "id:${contato.id}"
            nomeDisponivel != null -> "nome:${nomeDisponivel.lowercase()}"
            idResponsavel != null -> "idFallback:$idResponsavel"
            else -> "indice:$index"
        }

        val contatoResumoAssociado = when {
            contato?.idContato != null -> contatosPorIdContato[contato.idContato]
            idResponsavel != null -> contatosPorIdContato[idResponsavel]
            emailNormalizado != null -> contatosPorEmail[emailNormalizado]
            else -> null
        } ?: emailNormalizado?.let { contatosPorEmail[it] }

        val nomeContato = contatoResumoAssociado?.nome.trimmedOrNull()
        val emailContato = contatoResumoAssociado?.email.trimmedOrNull() ?: emailDisponivel

        val isUsuarioLogado = when {
            usuarioLogadoId != null && contato?.idContato == usuarioLogadoId -> true
            usuarioLogadoId != null && idResponsavel == usuarioLogadoId -> true
            emailUsuarioLogadoNormalizado != null && emailNormalizado == emailUsuarioLogadoNormalizado -> true
            else -> false
        }

        val displayText = when {
            isUsuarioLogado && nomeUsuarioLogado != null -> nomeUsuarioLogado
            nomeContato != null -> nomeContato
            emailContato != null -> emailContato
            nomeDisponivel != null -> nomeDisponivel
            idResponsavel != null -> "Responsável #$idResponsavel"
            else -> return null
        }

        val idFinal = idResponsavel ?: return null
        return chaveUnica to ResponsavelOption(id = idFinal, nome = displayText)
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

private fun String?.trimmedOrNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
