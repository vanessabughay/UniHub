package com.example.unihub.ui.HistoricoNotificacoes

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.R
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.repository.CompartilhamentoRepository
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.NotificationHistoryRepository
import com.example.unihub.notifications.CompartilhamentoNotificationSynchronizer
import com.example.unihub.notifications.ContatoNotificationManager
import com.example.unihub.notifications.ContatoNotificationSynchronizer
import java.time.Instant


import java.time.ZoneId
import java.time.format.DateTimeFormatter


import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

data class HistoricoNotificacaoUiModel(
    val id: Long,
    val titulo: String,
    val descricao: String,
    val dataHora: String,
    val referenceId: Long?,
    val hasPendingInteraction: Boolean,
    val tipo: String?,
    val categoria: String?
)


data class HistoricoNotificacoesUiState(
    val isLoading: Boolean = false,
    val notificacoes: List<HistoricoNotificacaoUiModel> = emptyList()
)

class HistoricoNotificacoesViewModel(
    application: Application,
    private val compartilhamentoRepository: CompartilhamentoRepository,
    private val contatoRepository: ContatoRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HistoricoNotificacoesUiState(isLoading = true))
    val uiState: StateFlow<HistoricoNotificacoesUiState> = _uiState.asStateFlow()

    private val _convitesProcessando = MutableStateFlow<Set<Long>>(emptySet())
    val convitesProcessando: StateFlow<Set<Long>> = _convitesProcessando.asStateFlow()

    private val _mensagens = MutableSharedFlow<String>()
    val mensagens: SharedFlow<String> = _mensagens

    private val dateFormatter = DateTimeFormatter.ofPattern(
        "dd/MM/yyyy 'às' HH:mm",
        Locale("pt", "BR")
    )
    private val historyZone: ZoneId = ZoneId.of("America/Sao_Paulo")

    private val appContext = application.applicationContext
    private val historyRepository = NotificationHistoryRepository.getInstance(appContext)

    init {
        observarHistorico()
        carregarHistorico(showLoading = true)
    }

    private fun observarHistorico() {
        viewModelScope.launch {
            historyRepository.historyFlow.collect { entries ->
                val models = entries
                    .sortedByDescending { it.timestampMillis }
                    .map { entry ->
                        HistoricoNotificacaoUiModel(
                            id = entry.id,
                            titulo = entry.title,
                            descricao = entry.message,
                            dataHora = formatTimestamp(entry.timestampMillis),
                            referenceId = entry.referenceId,
                            hasPendingInteraction = entry.hasPendingInteraction,
                            tipo = entry.type,
                            categoria = entry.category
                        )
                    }

                _uiState.value = HistoricoNotificacoesUiState(
                    isLoading = false,
                    notificacoes = models
                )
            }
        }
    }

    private fun carregarHistorico(showLoading: Boolean = false) {
        viewModelScope.launch {
            carregarHistoricoInterno(showLoading)
        }
    }

    private suspend fun carregarHistoricoInterno(showLoading: Boolean = false) {
        if (showLoading) {
            _uiState.update { it.copy(isLoading = true) }
        }

        TokenManager.loadToken(appContext, forceReload = true)
        val usuarioId = TokenManager.usuarioId
        if (usuarioId == null || usuarioId <= 0) {
            _uiState.value = HistoricoNotificacoesUiState(isLoading = false, notificacoes = emptyList())
            _mensagens.emit(appContext.getString(R.string.share_notification_action_session_expired))
            return
        }

        try {
            withContext(Dispatchers.IO) {
                CompartilhamentoNotificationSynchronizer.getInstance(appContext)
                    .refreshFromBackend(usuarioId)
                ContatoNotificationSynchronizer.getInstance(appContext)
                    .refreshFromBackend(usuarioId)
            }

            _uiState.update { current ->
                current.copy(isLoading = false)
            }

        } catch (exception: Exception) {
            _uiState.update { it.copy(isLoading = false) }
            val message = when (exception) {
                is HttpException -> when (exception.code()) {
                    401, 403 -> appContext.getString(R.string.share_notification_action_session_expired)
                    else -> appContext.getString(R.string.notification_history_load_error)
                }

                else -> exception.message ?: appContext.getString(R.string.notification_history_load_error)
            }
            _mensagens.emit(message)
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun aceitarConvite(conviteId: Long) {
        executarAcao(conviteId, aceitar = true)
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun rejeitarConvite(conviteId: Long) {
        executarAcao(conviteId, aceitar = false)
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun executarAcao(conviteId: Long, aceitar: Boolean) {
        TokenManager.loadToken(appContext, forceReload = true)
        val usuarioId = TokenManager.usuarioId
        if (usuarioId == null || usuarioId <= 0) {
            viewModelScope.launch {
                _mensagens.emit(appContext.getString(R.string.share_notification_action_session_expired))
            }
            return
        }

        atualizarProcessamento(conviteId, true)
        viewModelScope.launch {
            val historyEntry = historyRepository.historyFlow.value.firstOrNull { entry ->
                entry.referenceId == conviteId
            }
            val isShareInvite = historyEntry?.type?.equals(SHARE_INVITE_TYPE, ignoreCase = true) == true ||
                    historyEntry?.category?.equals(SHARE_CATEGORY, ignoreCase = true) == true
            val isContactInvite = historyEntry?.type?.equals(CONTACT_INVITE_TYPE, ignoreCase = true) == true ||
                    historyEntry?.category?.equals(CONTACT_CATEGORY, ignoreCase = true) == true
            var successMessageRes: Int? = null
            try {
                withContext(Dispatchers.IO) {
                    when {
                        isShareInvite -> {
                            if (aceitar) {
                                compartilhamentoRepository.aceitarConvite(conviteId, usuarioId)
                            } else {
                                compartilhamentoRepository.rejeitarConvite(conviteId, usuarioId)
                            }

                            val historyTitle = historyEntry?.title
                                ?: appContext.getString(R.string.share_notification_history_title)
                            val historyMessageRes = if (aceitar) {
                                R.string.share_notification_history_accept
                            } else {
                                R.string.share_notification_history_reject
                            }

                            historyRepository.updateShareInviteResponse(
                                referenceId = conviteId,
                                accepted = aceitar,
                                timestampMillis = System.currentTimeMillis(),
                                fallbackTitle = historyTitle,
                                fallbackMessage = historyEntry?.message
                            )

                            CompartilhamentoNotificationSynchronizer.getInstance(appContext)
                                .completeInvite(conviteId)
                            successMessageRes = historyMessageRes
                        }

                        isContactInvite -> {
                            if (aceitar) {
                                contatoRepository.acceptInvitation(conviteId)
                            } else {
                                contatoRepository.rejectInvitation(conviteId)
                            }

                            val historyTitle = historyEntry?.title
                                ?: appContext.getString(R.string.contact_notification_history_title)
                            val historyMessageRes = if (aceitar) {
                                R.string.contact_notification_history_accept
                            } else {
                                R.string.contact_notification_history_reject
                            }

                            historyRepository.updateContactNotification(
                                referenceId = conviteId,
                                title = historyTitle,
                                message = appContext.getString(historyMessageRes),
                                timestampMillis = System.currentTimeMillis(),
                                type = ContatoNotificationManager.TIPO_RESPOSTA,

                            )

                            ContatoNotificationSynchronizer.getInstance(appContext)
                                .completeInvite(conviteId)
                            successMessageRes = historyMessageRes
                        }

                        else -> throw IllegalStateException("Notificação não suportada para ação")
                    }

                    val historyEntry = historyRepository.historyFlow.value.firstOrNull { entry ->
                        entry.referenceId == conviteId &&
                                (entry.type?.equals(SHARE_INVITE_TYPE, ignoreCase = true) == true ||
                                        entry.category?.equals(SHARE_CATEGORY, ignoreCase = true) == true)
                    }


                }

                if (isShareInvite) {
                    CompartilhamentoNotificationSynchronizer.triggerImmediate(appContext)
                    CompartilhamentoNotificationSynchronizer.broadcastRefresh(appContext)
                }
                if (isContactInvite) {
                    ContatoNotificationSynchronizer.triggerImmediate(appContext)
                    ContatoNotificationSynchronizer.broadcastRefresh(appContext)
                }
                carregarHistoricoInterno()

                successMessageRes?.let { resId ->
                    _mensagens.emit(appContext.getString(resId))
                }
            } catch (exception: Exception) {
                _mensagens.emit(
                    exception.message ?: appContext.getString(
                        if (isContactInvite) {
                            R.string.contact_notification_action_error
                        } else {
                            R.string.share_notification_action_error
                        }
                    )
                )
            } finally {
                atualizarProcessamento(conviteId, false)
            }
        }
    }

    private fun atualizarProcessamento(conviteId: Long, emProcessamento: Boolean) {
        val atual = _convitesProcessando.value.toMutableSet()
        if (emProcessamento) {
            atual.add(conviteId)
        } else {
            atual.remove(conviteId)
        }
        _convitesProcessando.value = atual
    }
    private fun formatTimestamp(timestampMillis: Long): String {
        val zonedDateTime = Instant.ofEpochMilli(timestampMillis)
            .atZone(historyZone)
        return dateFormatter.format(zonedDateTime)
    }
}

private const val SHARE_INVITE_TYPE = "DISCIPLINA_COMPARTILHAMENTO"
private const val SHARE_CATEGORY = "COMPARTILHAMENTO"
private const val CONTACT_INVITE_TYPE = "CONTATO_SOLICITACAO"
private const val CONTACT_CATEGORY = "CONTATO"