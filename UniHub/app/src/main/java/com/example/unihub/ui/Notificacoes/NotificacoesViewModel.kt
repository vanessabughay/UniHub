package com.example.unihub.ui.Notificacoes

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Antecedencia
import com.example.unihub.data.model.AvaliacoesConfig
import com.example.unihub.data.model.NotificacoesConfig
import com.example.unihub.data.model.Prioridade
import com.example.unihub.data.model.deepCopy
import com.example.unihub.data.model.normalized
import com.example.unihub.data.repository.NotificacoesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificacoesUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val original: NotificacoesConfig = NotificacoesConfig(),
    val edit: NotificacoesConfig = NotificacoesConfig(),
    val disciplinasExpandido: Boolean = true,
    val quadrosExpandido: Boolean = false,
    val contatosExpandido: Boolean = false,
    val botaoSalvarHabilitado: Boolean = false,
    val configuracoesSalvas: Boolean = false
)

class NotificacoesViewModel(
    private val repository: NotificacoesRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(NotificacoesUiState())
    val ui: StateFlow<NotificacoesUiState> = _ui.asStateFlow()

    init {
        carregar()
    }

    fun carregar() = viewModelScope.launch {
        _ui.value = _ui.value.copy(isLoading = true, error = null)
        try {
            val config = repository.carregarConfig().normalized()
            val original = config.deepCopy()
            val edit = config.deepCopy()
            _ui.value = NotificacoesUiState(
                isLoading = false,
                original = original,
                edit = edit,
                disciplinasExpandido = false,
                quadrosExpandido = false,
                contatosExpandido = false,
                botaoSalvarHabilitado = false,
                configuracoesSalvas = false

            )
        } catch (t: Throwable) {
            _ui.value = _ui.value.copy(isLoading = false, error = t.message ?: "Erro ao carregar")
        }
    }


    fun alternarDisciplinasExpandido() {
        _ui.value = _ui.value.copy(disciplinasExpandido = !_ui.value.disciplinasExpandido)
    }

    fun alternarQuadrosExpandido() {
        _ui.value = _ui.value.copy(quadrosExpandido = !_ui.value.quadrosExpandido)
    }

    fun alternarContatosExpandido() {
        _ui.value = _ui.value.copy(contatosExpandido = !_ui.value.contatosExpandido)
    }


    fun setPresenca(ativo: Boolean) = update { it.copy(notificacaoDePresenca = ativo) }

    fun setAvaliacoesAtivas(ativo: Boolean) = update { it.copy(avaliacoesAtivas = ativo) }

    fun setAntecedencia(p: Prioridade, a: Antecedencia) = update {
        val novoMapa = it.avaliacoesConfig.antecedencia.toMutableMap()
        novoMapa[p] = a
        it.copy(avaliacoesConfig = AvaliacoesConfig(antecedencia = novoMapa))
    }

    fun setCompartilhamentoDisciplina(ativo: Boolean) = update { it.copy(compartilhamentoDisciplina = ativo) }
    fun setIncluirEmQuadro(ativo: Boolean) = update { it.copy(incluirEmQuadro = ativo) }
    fun setPrazoTarefa(ativo: Boolean) = update { it.copy(prazoTarefa = ativo) }
    fun setComentarioTarefa(ativo: Boolean) = update { it.copy(comentarioTarefa = ativo) }
    fun setConviteContato(ativo: Boolean) = update { it.copy(conviteContato = ativo) }
    fun setInclusoEmGrupo(ativo: Boolean) = update { it.copy(inclusoEmGrupo = ativo) }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun salvar() = viewModelScope.launch {
        val edit = _ui.value.edit
        _ui.value = _ui.value.copy(isLoading = true, error = null, configuracoesSalvas = false)
        try {
            // O 'edit' (NotificacoesConfig) agora contém TODOS os toggles
            val salvo = repository.salvarConfig(edit)
            val snapshot = salvo.deepCopy()
            _ui.value = _ui.value.copy(
                isLoading = false,
                original = snapshot,
                edit = snapshot,
                botaoSalvarHabilitado = false,
                configuracoesSalvas = true
            )
        } catch (t: Throwable) {
            _ui.value = _ui.value.copy(
                isLoading = false,
                error = t.message ?: "Erro ao salvar",
                configuracoesSalvas = false
            )
        }
    }
    fun confirmarConfiguracoesSalvas() {
        _ui.value = _ui.value.copy(configuracoesSalvas = false)
    }

    /**
     * Helper que atualiza o estado 'edit' e calcula se o botão 'Salvar'
     * deve ficar habilitado (comparando 'edit' com 'original').
     */
    private fun update(block: (NotificacoesConfig) -> NotificacoesConfig) {
        val next = block(_ui.value.edit).normalized()
        val changed = next != _ui.value.original
        _ui.value = _ui.value.copy(edit = next, botaoSalvarHabilitado = changed)
    }
}