package com.example.unihub.ui.Notificacoes

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Antecedencia
import com.example.unihub.data.model.AvaliacoesConfig
import com.example.unihub.data.model.NotificacoesConfig
import com.example.unihub.data.model.Prioridade
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
    val compartilhamentoDisciplina: Boolean = true, // Notificação de Compartilhamento
    val incluirEmQuadro: Boolean = true, // Notificação de Inclusão em Quadro
    val prazoTarefa: Boolean = true, // Notificação de Prazo de Tarefa
    val comentarioTarefa: Boolean = true, // Notificação de Comentário em Tarefa
    val conviteContato: Boolean = true, // Notificação de Convite de Contato
    val inclusoEmGrupo: Boolean = true // Notificação de Inclusão em Grupo
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
            val config = repository.carregarConfig()
            _ui.value = NotificacoesUiState(
                isLoading = false,
                original = config,
                edit = config,
                disciplinasExpandido = false,
                quadrosExpandido = false,
                contatosExpandido = false,
                botaoSalvarHabilitado = false,
                compartilhamentoDisciplina = true,
                incluirEmQuadro = true,
                prazoTarefa = true,
                comentarioTarefa = true,
                conviteContato = true,
                inclusoEmGrupo = true
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
        val novoMapa = it.avaliacoesConfig.periodicidade.toMutableMap()
        novoMapa[p] = a
        it.copy(avaliacoesConfig = AvaliacoesConfig(periodicidade = novoMapa))
    }

    fun setCompartilhamentoDisciplina(ativo: Boolean) = _ui.update { it.copy(compartilhamentoDisciplina = ativo, botaoSalvarHabilitado = true) }
    fun setIncluirEmQuadro(ativo: Boolean) = _ui.update { it.copy(incluirEmQuadro = ativo, botaoSalvarHabilitado = true) }
    fun setPrazoTarefa(ativo: Boolean) = _ui.update { it.copy(prazoTarefa = ativo, botaoSalvarHabilitado = true) }
    fun setComentarioTarefa(ativo: Boolean) = _ui.update { it.copy(comentarioTarefa = ativo, botaoSalvarHabilitado = true) }
    fun setConviteContato(ativo: Boolean) = _ui.update { it.copy(conviteContato = ativo, botaoSalvarHabilitado = true) }
    fun setInclusoEmGrupo(ativo: Boolean) = _ui.update { it.copy(inclusoEmGrupo = ativo, botaoSalvarHabilitado = true) }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun salvar() = viewModelScope.launch {
        val edit = _ui.value.edit
        _ui.value = _ui.value.copy(isLoading = true, error = null)
        try {
            repository.salvarConfig(edit)
            _ui.value = _ui.value.copy(
                isLoading = false,
                original = edit,
                botaoSalvarHabilitado = false
            )
        } catch (t: Throwable) {
            _ui.value = _ui.value.copy(isLoading = false, error = t.message ?: "Erro ao salvar")
        }
    }

    private fun update(block: (NotificacoesConfig) -> NotificacoesConfig) {
        val next = block(_ui.value.edit)
        val changed = next != _ui.value.original
        _ui.value = _ui.value.copy(edit = next, botaoSalvarHabilitado = changed)
    }
}