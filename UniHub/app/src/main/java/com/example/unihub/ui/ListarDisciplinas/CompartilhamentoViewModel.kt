package com.example.unihub.ui.ListarDisciplinas

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.apiBackend.ApiCompartilhamentoBackend
import com.example.unihub.data.model.CompartilharDisciplinaRequest
import com.example.unihub.data.model.NotificacaoResponse
import com.example.unihub.data.model.UsuarioResumo
import com.example.unihub.data.repository.CompartilhamentoBackend
import com.example.unihub.data.repository.CompartilhamentoRepository
import com.example.unihub.data.repository.NotificationHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

data class NotificacaoConviteUi(
    val id: Long,
    val titulo: String?,
    val mensagem: String,
    val conviteId: Long?,
    val tipo: String?,
    val lida: Boolean,
    val criadaEm: String?,
    val categoria: String?,
    val referenciaId: Long?,
    val interacaoPendente: Boolean,
    val metadataJson: String?,
    val atualizadaEm: String?
)

class CompartilhamentoViewModel(
    private val repository: CompartilhamentoRepository,
    private val notificationHistoryRepository: NotificationHistoryRepository
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
                _erro.value = when (e) {
                    is IllegalStateException -> e.message ?: "Sessão expirada. Faça login novamente."
                    else -> e.message ?: "Erro ao carregar contatos"
                }
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
        destinatarioId: Long
    ) {
        if (_isCompartilhando.value) return
        if (usuarioId == destinatarioId) {
            _erro.value = "Você não pode compartilhar uma disciplina consigo mesmo"
            return
        }
        viewModelScope.launch {
            _isCompartilhando.value = true
            try {
                val request = CompartilharDisciplinaRequest(
                    disciplinaId = disciplinaId,
                    destinatarioId = destinatarioId
                )
                repository.compartilhar(request)
                _statusMessage.value = "Convite enviado com sucesso"
            } catch (e: Exception) {
                _erro.value = when (e) {
                    is HttpException -> when (e.code()) {
                        400 -> "Não foi possível compartilhar: verifique os dados e tente novamente"
                        401, 403 -> "Acesso negado. Faça login novamente."
                        else -> e.message()
                    }
                    is IllegalStateException -> e.message ?: "Sessão expirada. Faça login novamente."
                    else -> e.message ?: "Erro ao compartilhar disciplina"
                }
            } finally {
                _isCompartilhando.value = false
            }
        }
    }

    fun aceitarConvite(usuarioId: Long, conviteId: Long) {
        atualizarProcessamento(conviteId, true)
        val notificacao = _notificacoes.value.firstOrNull {
            it.conviteId == conviteId || it.referenciaId == conviteId
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.aceitarConvite(conviteId, usuarioId)
                    notificationHistoryRepository.updateShareInviteResponse(
                        referenceId = conviteId,
                        accepted = true,
                        timestampMillis = System.currentTimeMillis(),
                        fallbackTitle = notificacao?.titulo,
                        fallbackMessage = notificacao?.mensagem
                    )
                }
                carregarNotificacoes(usuarioId)
                _statusMessage.value = "Convite aceito"
            } catch (e: Exception) {
                _erro.value = when (e) {
                    is IllegalStateException -> e.message ?: "Sessão expirada. Faça login novamente."
                    else -> e.message ?: "Erro ao aceitar convite"
                }
            } finally {
                atualizarProcessamento(conviteId, false)
            }
        }
    }

    fun rejeitarConvite(usuarioId: Long, conviteId: Long) {
        atualizarProcessamento(conviteId, true)
        val notificacao = _notificacoes.value.firstOrNull {
            it.conviteId == conviteId || it.referenciaId == conviteId
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.rejeitarConvite(conviteId, usuarioId)
                    notificationHistoryRepository.updateShareInviteResponse(
                        referenceId = conviteId,
                        accepted = false,
                        timestampMillis = System.currentTimeMillis(),
                        fallbackTitle = notificacao?.titulo,
                        fallbackMessage = notificacao?.mensagem
                    )
                }
                carregarNotificacoes(usuarioId)
                _statusMessage.value = "Convite rejeitado"
            } catch (e: Exception) {
                _erro.value = when (e) {
                    is IllegalStateException -> e.message ?: "Sessão expirada. Faça login novamente."
                    else -> e.message ?: "Erro ao rejeitar convite"
                }
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
        titulo = titulo,
        mensagem = mensagem,
        conviteId = conviteId,
        tipo = tipo,
        lida = lida,
        criadaEm = criadaEm,
        categoria = categoria,
        referenciaId = referenciaId,
        interacaoPendente = interacaoPendente,
        metadataJson = metadataJson,
        atualizadaEm = atualizadaEm
    )
}

class CompartilhamentoViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompartilhamentoViewModel::class.java)) {
            val backend: CompartilhamentoBackend = ApiCompartilhamentoBackend()
            val repository = CompartilhamentoRepository(backend)
            val historyRepository = NotificationHistoryRepository.getInstance(context)
            return CompartilhamentoViewModel(repository, historyRepository) as T
        }
        throw IllegalArgumentException("Classe de ViewModel desconhecida")
    }
}