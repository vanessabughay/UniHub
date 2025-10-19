package com.example.unihub.ui.ManterQuadro

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                        .map { ContatoResumoUi(it.id, it.nome) }
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

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        disciplinasDisponiveis = disciplinas,
                        contatosDisponiveis = contatos,
                        gruposDisponiveis = grupos,
                        quadroSendoEditado = quadro,
                        nome = quadro?.nome ?: "",
                        estado = quadro?.estado ?: Estado.ATIVO,
                        prazo = quadro?.dataFim ?: System.currentTimeMillis(),
                        disciplinaSelecionada = disciplinaSel,
                        integranteSelecionado = integranteSel
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Falha ao carregar dados: ${e.message}") }
            }
        }
    }

    fun onNomeChange(nome: String) { _uiState.update { it.copy(nome = nome) } }
    fun onEstadoChange(estado: Estado) { _uiState.update { it.copy(estado = estado) } }
    fun onPrazoChange(prazo: Long) { _uiState.update { it.copy(prazo = prazo) } }
    fun onDisciplinaSelecionada(disciplina: DisciplinaResumoUi?) { _uiState.update { it.copy(disciplinaSelecionada = disciplina) } }
    fun onIntegranteSelecionado(integrante: IntegranteUi?) { _uiState.update { it.copy(integranteSelecionado = integrante, isSelectionDialogVisible = false) } }
    fun onSelectionDialogShow() { _uiState.update { it.copy(isSelectionDialogVisible = true) } }
    fun onSelectionDialogDismiss() { _uiState.update { it.copy(isSelectionDialogVisible = false) } }

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
                    dataInicio = existingQuadro?.dataInicio ?: System.currentTimeMillis(),
                    dataFim = currentState.prazo,
                    donoId = existingQuadro?.donoId,
                    colunas = existingQuadro?.colunas ?: emptyList()
                )

                if (existingQuadro != null) {
                    quadroToSave = quadroToSave.copy(
                        dataInicio = existingQuadro.dataInicio,
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