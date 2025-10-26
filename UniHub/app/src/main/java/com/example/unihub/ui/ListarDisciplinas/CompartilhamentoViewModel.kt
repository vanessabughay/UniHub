package com.example.unihub.ui.ListarDisciplinas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.apiBackend.ApiCompartilhamentoBackend
import com.example.unihub.data.model.CompartilharDisciplinaRequest
import com.example.unihub.data.model.NotificacaoResponse
import com.example.unihub.data.model.UsuarioResumo
import com.example.unihub.data.repository.CompartilhamentoBackend
import com.example.unihub.data.repository.CompartilhamentoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificacaoConviteUi(
    val id: Long,
    val mensagem: String,
    val conviteId: Long?,
    val tipo: String?,
    val lida: Boolean,
    val criadaEm: String?
)

class CompartilhamentoViewModel(
    private val repository: CompartilhamentoRepository
) : ViewModel() {

    private val _contatos = MutableStateFlow<List<UsuarioResumo>>(emptyList())
    val contatos: StateFlow<List<UsuarioResumo>> = _contatos.asStateFlow()

    private val _isCarregandoContatos = MutableStateFlow(false)
    val isCarregandoContatos: StateFlow<Boolean> = _isCarregandoContatos.asStateFlow()

    private val _notificacoes = MutableStateFlow<List<NotificacaoConviteUi>>(emptyList())
    val notificacoes: StateFlow<List<NotificacaoConviteUi>> = _notificacoes.asStateFlow()

    private val _convitesEmProcessamento = MutableStateFlow<Set<Long>>(emptySet())
    val convitesEmProcessamento: StateFlow<Set<Long>> = _convitesEmProcessamento.asStateFlow()

    private val _isCompartilhando = MutableStateFlow(false)
    val isCompartilhando: StateFlow<Boolean> = _isCompartilhando.asStateFlow()

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun carregarContatos(usuarioId: Long) {
        if (usuarioId <= 0) {
            _erro.value = "Usuário inválido"
            return
        }
        viewModelScope.launch {
            _isCarregandoContatos.value = true
            try {
                _contatos.value = repository.listarContatos(usuarioId)
            } catch (e: Exception) {
                _erro.value = e.message ?: "Erro ao carregar contatos"
            } finally {
                _isCarregandoContatos.value = false
            }
        }
    }

    fun carregarNotificacoes(usuarioId: Long) {
        if (usuarioId <= 0) {
            return
        }
        viewModelScope.launch {
            try {
                val respostas = repository.listarNotificacoes(usuarioId)
                _notificacoes.value = respostas.map { it.toUi() }
            } catch (e: Exception) {
                _erro.value = e.message ?: "Erro ao carregar notificações"
            }
        }
    }

    fun compartilharDisciplina(
        usuarioId: Long,
        disciplinaId: Long,
        destinatarioId: Long,
        mensagem: String?
    ) {
        if (_isCompartilhando.value) return
        viewModelScope.launch {
            _isCompartilhando.value = true
            try {
                val request = CompartilharDisciplinaRequest(
                    disciplinaId = disciplinaId,
                    remetenteId = usuarioId,
                    destinatarioId = destinatarioId,
                    mensagem = mensagem?.takeIf { it.isNotBlank() }
                )
                repository.compartilhar(request)
                _statusMessage.value = "Convite enviado com sucesso"
            } catch (e: Exception) {
                _erro.value = e.message ?: "Erro ao compartilhar disciplina"
            } finally {
                _isCompartilhando.value = false
            }
        }
    }

    fun aceitarConvite(usuarioId: Long, conviteId: Long) {
        atualizarProcessamento(conviteId, true)
        viewModelScope.launch {
            try {
                repository.aceitarConvite(conviteId, usuarioId)
                carregarNotificacoes(usuarioId)
                _statusMessage.value = "Convite aceito"
            } catch (e: Exception) {
                _erro.value = e.message ?: "Erro ao aceitar convite"
            } finally {
                atualizarProcessamento(conviteId, false)
            }
        }
    }

    fun rejeitarConvite(usuarioId: Long, conviteId: Long) {
        atualizarProcessamento(conviteId, true)
        viewModelScope.launch {
            try {
                repository.rejeitarConvite(conviteId, usuarioId)
                carregarNotificacoes(usuarioId)
                _statusMessage.value = "Convite rejeitado"
            } catch (e: Exception) {
                _erro.value = e.message ?: "Erro ao rejeitar convite"
            } finally {
                atualizarProcessamento(conviteId, false)
            }
        }
    }

    fun consumirErro() {
        _erro.value = null
    }

    fun consumirStatus() {
        _statusMessage.value = null
    }

    private fun atualizarProcessamento(conviteId: Long, emProcessamento: Boolean) {
        val atual = _convitesEmProcessamento.value.toMutableSet()
        if (emProcessamento) {
            atual.add(conviteId)
        } else {
            atual.remove(conviteId)
        }
        _convitesEmProcessamento.value = atual
    }

    private fun NotificacaoResponse.toUi(): NotificacaoConviteUi = NotificacaoConviteUi(
        id = id,
        mensagem = mensagem,
        conviteId = conviteId,
        tipo = tipo,
        lida = lida,
        criadaEm = criadaEm
    )
}

object CompartilhamentoViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompartilhamentoViewModel::class.java)) {
            val backend: CompartilhamentoBackend = ApiCompartilhamentoBackend()
            val repository = CompartilhamentoRepository(backend)
            return CompartilhamentoViewModel(repository) as T
        }
        throw IllegalArgumentException("Classe de ViewModel desconhecida")
    }
}