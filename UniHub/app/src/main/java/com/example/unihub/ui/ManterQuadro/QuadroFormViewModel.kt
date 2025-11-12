package com.example.unihub.ui.ManterQuadro

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.model.Estado
import com.example.unihub.data.model.Quadro
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.data.repository.GrupoRepository
import com.example.unihub.data.repository.QuadroRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed class FormResult {
    object Idle : FormResult()
    object Success : FormResult()
    data class Error(val message: String) : FormResult()
}

class QuadroFormViewModel(
    private val quadroRepository: QuadroRepository,
    private val disciplinaRepository: DisciplinaRepository,
    private val contatoRepository: ContatoRepository,
    private val grupoRepository: GrupoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuadroFormUiState())
    val uiState: StateFlow<QuadroFormUiState> = _uiState.asStateFlow()

    private val _formResult = MutableStateFlow<FormResult>(FormResult.Idle)
    val formResult: StateFlow<FormResult> = _formResult.asStateFlow()

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun carregarDados(quadroId: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val disciplinasAsync = async { disciplinaRepository.getDisciplinasResumo().first().map { DisciplinaResumoUi(it.id, it.nome) } }
                val contatosAsync = async {
                    contatoRepository
                        .getContatoResumo()
                        .first()
                        .filter { !it.pendente }
                        .map {
                            ContatoResumoUi(
                                id = it.id,
                                nome = it.nome,
                                email = it.email,
                                ownerId = it.ownerId
                            )
                        }
                }
                val gruposAsync = async { grupoRepository.getGrupo().first().map { GrupoResumoUi(it.id, it.nome) } }

                val disciplinas = disciplinasAsync.await()
                val contatos = contatosAsync.await()
                val grupos = gruposAsync.await()

                var quadro: Quadro? = null
                if (quadroId != null) {
                    quadro = quadroRepository.getQuadroById(quadroId)
                }

                val idDisciplinaProcurado = quadro?.disciplinaId
                val idContatoProcurado = quadro?.contatoId
                val idGrupoProcurado = quadro?.grupoId

                val disciplinaSel = if (idDisciplinaProcurado != null) disciplinas.find { it.id == idDisciplinaProcurado } else null
                val integranteSel = if (idContatoProcurado != null) {
                    contatos.find { it.id == idContatoProcurado }?.let { ContatoIntegranteUi(it.id, it.nome) }
                } else if (idGrupoProcurado != null) {
                    grupos.find { it.id == idGrupoProcurado }?.let { GrupoIntegranteUi(it.id, it.nome) }
                } else { null }

                val integrantesDoQuadro = buildIntegrantesDoQuadro(
                    integranteSelecionado = integranteSel,
                    quadroOriginal = quadro,
                    contatosDisponiveis = contatos
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        disciplinasDisponiveis = disciplinas,
                        contatosDisponiveis = contatos,
                        gruposDisponiveis = grupos,
                        quadroSendoEditado = quadro,
                        nome = quadro?.nome ?: "",
                        estado = quadro?.estado ?: Estado.ATIVO,
                        prazo = quadro?.dataFim,
                        disciplinaSelecionada = disciplinaSel,
                        integranteSelecionado = integranteSel,
                        integrantesDoQuadro = integrantesDoQuadro
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    val nomeUsuario = TokenManager.nomeUsuario?.takeIf { it.isNotBlank() } ?: "Você"
                    val participantesFallback = "$nomeUsuario (Administrador do quadro)"
                    val integrantesFallback = if (it.integrantesDoQuadro.participantes.isEmpty() && it.integrantesDoQuadro.grupo == null) {
                        IntegrantesDoQuadroUi(participantes = listOf(participantesFallback))
                    } else {
                        it.integrantesDoQuadro
                    }
                    it.copy(
                        isLoading = false,
                        error = "Falha ao carregar dados: ${e.message}",
                        integrantesDoQuadro = integrantesFallback
                    )
                }
            }
        }
    }

    fun onNomeChange(nome: String) { _uiState.update { it.copy(nome = nome) } }
    fun onEstadoChange(estado: Estado) { _uiState.update { it.copy(estado = estado) } }
    fun onPrazoChange(prazo: Long?) { _uiState.update { it.copy(prazo = prazo) } }
    fun onDisciplinaSelecionada(disciplina: DisciplinaResumoUi?) { _uiState.update { it.copy(disciplinaSelecionada = disciplina) } }
    fun onIntegranteSelecionado(integrante: IntegranteUi?) {
        viewModelScope.launch {
            val integrantesDoQuadro = buildIntegrantesDoQuadro(
                integranteSelecionado = integrante,
                quadroOriginal = _uiState.value.quadroSendoEditado,
                contatosDisponiveis = _uiState.value.contatosDisponiveis
            )
            _uiState.update {
                it.copy(
                    integranteSelecionado = integrante,
                    isSelectionDialogVisible = false,
                    integrantesDoQuadro = integrantesDoQuadro
                )
            }
        }
    }
    fun onSelectionDialogShow() { _uiState.update { it.copy(isSelectionDialogVisible = true) } }
    fun onSelectionDialogDismiss() { _uiState.update { it.copy(isSelectionDialogVisible = false) } }

    private suspend fun buildIntegrantesDoQuadro(
        integranteSelecionado: IntegranteUi?,
        quadroOriginal: Quadro?,
        contatosDisponiveis: List<ContatoResumoUi>
    ): IntegrantesDoQuadroUi {
        val participantes = mutableListOf<String>()
        val usuarioId = TokenManager.usuarioId
        val nomeUsuario = TokenManager.nomeUsuario?.takeIf { it.isNotBlank() }

        val ownerId = quadroOriginal?.donoId ?: usuarioId
        var ownerNameFromGrupo: String? = null

        val contatoId = when (integranteSelecionado) {
            is ContatoIntegranteUi -> integranteSelecionado.id
            else -> quadroOriginal?.contatoId
        }

        val contatoNome = when (integranteSelecionado) {
            is ContatoIntegranteUi -> integranteSelecionado.nome
            else -> null
        } ?: contatoId?.let { id ->
            contatosDisponiveis.firstOrNull { it.id == id }?.nome ?: runCatching {
                contatoRepository.fetchContatoById(id)?.nome
            }.getOrNull()
        }

        if (!contatoNome.isNullOrBlank()) {
            participantes += contatoNome
        }

        val grupoId = when (integranteSelecionado) {
            is GrupoIntegranteUi -> integranteSelecionado.id
            else -> quadroOriginal?.grupoId
        }

        val grupoDetalhes = grupoId?.let { id ->
            val grupoResult = runCatching { grupoRepository.fetchGrupoById(id) }
            val grupo = grupoResult.getOrNull()
            val membros = if (grupo != null) {
                val membrosOrdenados = grupo.membros
                    .sortedBy { contato -> contato.id ?: Long.MAX_VALUE }

                val usuarioLogadoId = TokenManager.usuarioId
                val nomeUsuarioLogado = TokenManager.nomeUsuario
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                val emailUsuarioLogadoNormalizado = TokenManager.emailUsuario
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?.lowercase()

                val contatosDoUsuario = if (usuarioLogadoId != null) {
                    contatosDisponiveis.filter { resumo ->
                        resumo.ownerId == usuarioLogadoId
                    }
                } else {
                    emptyList()
                }
                val contatosPorEmail = contatosDoUsuario
                    .mapNotNull { resumo ->
                        resumo.email
                            ?.trim()
                            ?.takeIf { it.isNotEmpty() }
                            ?.lowercase()
                            ?.let { it to resumo }
                    }
                    .toMap()

                val contatosPorIdContato = contatosDoUsuario
                    .mapNotNull { resumo -> resumo.id?.let { it to resumo } }
                    .toMap()

                val membrosExibidos = linkedSetOf<String>()
                val membrosFormatados = mutableListOf<String>()

                membrosOrdenados.forEachIndexed { index, contato ->
                    val emailDisponivel = contato.email?.trim()?.takeIf { it.isNotEmpty() }
                    val emailNormalizado = emailDisponivel?.lowercase()

                    val chaveUnica = contato.idContato?.let { "idContato:$it" }
                        ?: emailNormalizado?.let { "email:$it" }
                        ?: contato.id?.let { "id:$it" }
                        ?: contato.nome?.trim()?.takeIf { it.isNotEmpty() }?.lowercase()
                            ?.let { "nome:$it" }
                        ?: "indice:$index"

                    if (!membrosExibidos.add(chaveUnica)) {
                        return@forEachIndexed
                    }

                    val contatoResumoAssociado = contato.idContato?.let { contatosPorIdContato[it] }
                        ?: emailNormalizado?.let { contatosPorEmail[it] }

                    val nomeContato = contatoResumoAssociado?.nome
                        ?.trim()
                        ?.takeIf { it.isNotEmpty() }

                    val emailContato = contatoResumoAssociado?.email
                        ?.trim()
                        ?.takeIf { it.isNotEmpty() }
                        ?: emailDisponivel

                    val isUsuarioLogado = (
                            usuarioLogadoId != null &&
                                    contato.idContato != null &&
                                    contato.idContato == usuarioLogadoId
                            ) || (
                            emailUsuarioLogadoNormalizado != null &&
                                    emailNormalizado == emailUsuarioLogadoNormalizado
                            )

                    val textoBase = when {
                        isUsuarioLogado && nomeUsuarioLogado != null -> nomeUsuarioLogado
                        nomeContato != null -> nomeContato
                        emailContato != null -> emailContato
                        else -> contato.nome?.trim()?.takeIf { it.isNotEmpty() }
                            ?: "Sem identificação"
                    }

                    val isAdministrador = grupo.ownerId != null &&
                            contato.idContato != null &&
                            contato.idContato == grupo.ownerId

                    if (isAdministrador && ownerNameFromGrupo == null) {
                        ownerNameFromGrupo = textoBase
                    }

                    val rotuloAdministrador = if (isAdministrador) {
                        " (Administrador do Grupo)"
                    } else {
                        ""
                    }

                    membrosFormatados += "- $textoBase$rotuloAdministrador"
                }



                when {
                    membrosFormatados.isNotEmpty() -> membrosFormatados
                    else -> listOf("Nenhum integrante neste grupo.")
                }
            } else if (grupoResult.isFailure) {
                listOf("Não foi possível carregar os integrantes do grupo.")
            } else {
                emptyList()
            }

            GrupoDetalhesUi(
                nome = (integranteSelecionado as? GrupoIntegranteUi)?.nome
                    ?: grupo?.nome
                    ?: "Grupo #$id",
                membros = membros
            )
        }

        if (ownerId != null) {
            val ownerNomeBase = when {
                ownerId == usuarioId && !nomeUsuario.isNullOrBlank() -> nomeUsuario
                else -> null
            }

            val ownerNome = ownerNomeBase
                ?: contatosDisponiveis.firstOrNull { it.id == ownerId }?.nome?.takeIf { !it.isNullOrBlank() }
                ?: runCatching { contatoRepository.fetchContatoById(ownerId)?.nome }
                    .getOrNull()
                    ?.takeIf { !it.isNullOrBlank() }
                ?: ownerNameFromGrupo
                ?: "Usuário #$ownerId"

            val ownerRotulo = buildString {
                append(ownerNome)
                append(" (Administrador do quadro)")
            }
            participantes.add(0, ownerRotulo)
        } else {
            val nomeExibicao = nomeUsuario ?: "Você"
            participantes.add(0, "$nomeExibicao (Administrador do quadro)")
        }

        return IntegrantesDoQuadroUi(
            participantes = participantes.distinct(),
            grupo = grupoDetalhes
        )
    }

    fun salvarOuAtualizarQuadro() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.nome.isBlank()) {
                _formResult.value = FormResult.Error("O nome do quadro é obrigatório.")
                return@launch
            }

            try {
                val existingQuadro = currentState.quadroSendoEditado

                var quadroToSave = Quadro(
                    id = existingQuadro?.id,
                    nome = currentState.nome,
                    disciplinaId = currentState.disciplinaSelecionada?.id,
                    contatoId = (currentState.integranteSelecionado as? ContatoIntegranteUi)?.id,
                    grupoId = (currentState.integranteSelecionado as? GrupoIntegranteUi)?.id,
                    estado = currentState.estado,
                    dataFim = currentState.prazo,
                    donoId = existingQuadro?.donoId,
                    colunas = existingQuadro?.colunas ?: emptyList()
                )

                if (existingQuadro != null) {
                    quadroToSave = quadroToSave.copy(
                        donoId = existingQuadro.donoId
                    )
                }

                if (quadroToSave.estado == Estado.INATIVO && existingQuadro?.estado != Estado.INATIVO && quadroToSave.dataFim == null) {
                    quadroToSave = quadroToSave.copy(dataFim = System.currentTimeMillis())
                } else if (quadroToSave.estado != Estado.INATIVO && existingQuadro?.estado == Estado.INATIVO && quadroToSave.dataFim == existingQuadro.dataFim) {
                    quadroToSave = quadroToSave.copy(dataFim = null)
                }

                if (!quadroToSave.id.isNullOrBlank()) {
                    quadroRepository.updateQuadro(quadroToSave)
                } else {
                    quadroRepository.addQuadro(quadroToSave)
                }

                _formResult.value = FormResult.Success
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    401, 403 -> "Sua sessão expirou. Faça login novamente para continuar."
                    else -> e.message()
                } ?: "Erro do servidor ao salvar/atualizar quadro."
                _formResult.value = FormResult.Error(message)
            } catch (e: Exception) {
                _formResult.value = FormResult.Error(e.message ?: "Erro desconhecido ao salvar/atualizar quadro.")
            }
        }
    }

    fun excluirQuadro(quadroId: String) {
        viewModelScope.launch {
            try {
                quadroRepository.deleteQuadro(quadroId)
                _formResult.value = FormResult.Success
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    401, 403 -> "Sua sessão expirou. Faça login novamente para continuar."
                    else -> e.message()
                } ?: "Erro do servidor ao excluir quadro."
                _formResult.value = FormResult.Error(message)
            } catch (e: Exception) {
                _formResult.value = FormResult.Error(e.message ?: "Erro ao excluir")
            }
        }
    }

    fun resetFormResult() {
        _formResult.value = FormResult.Idle
    }
}