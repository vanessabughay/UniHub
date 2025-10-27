package com.example.unihub.ui.HistoricoNotificacoes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.repository.NotificationHistoryRepository
import com.example.unihub.data.repository.CompartilhamentoRepository
import com.example.unihub.data.config.TokenManager
import com.example.unihub.notifications.CompartilhamentoNotificationSynchronizer
import com.example.unihub.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class HistoricoNotificacaoUiModel(
    val id: Long,
    val titulo: String,
    val descricao: String,
    val dataHora: String,
    val shareInviteId: Long?,
    val shareActionPending: Boolean
)


data class HistoricoNotificacoesUiState(
    val isLoading: Boolean = false,
    val notificacoes: List<HistoricoNotificacaoUiModel> = emptyList()
)

class HistoricoNotificacoesViewModel(
    application: Application,
    private val repository: NotificationHistoryRepository,
    private val compartilhamentoRepository: CompartilhamentoRepository
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
    private val appContext = application.applicationContext

    init {
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            repository.historyFlow.collect { entries ->
                val models = entries.map { entry ->
                    HistoricoNotificacaoUiModel(
                        id = entry.id,
                        titulo = entry.title,
                        descricao = entry.message,
                        dataHora = formatTimestamp(entry.timestampMillis),
                        shareInviteId = entry.shareInviteId,
                        shareActionPending = entry.shareActionPending
                    )
                }

                _uiState.value = HistoricoNotificacoesUiState(
                    isLoading = false,
                    notificacoes = models
                )
            }
        }
    }

    fun aceitarConvite(conviteId: Long) {
        executarAcao(conviteId, true)
    }

    fun rejeitarConvite(conviteId: Long) {
        executarAcao(conviteId, false)
    }

    private fun executarAcao(conviteId: Long, aceitar: Boolean) {
        val usuarioId = TokenManager.usuarioId
        if (usuarioId == null || usuarioId <= 0) {
            viewModelScope.launch {
                _mensagens.emit(appContext.getString(R.string.share_notification_action_session_expired))
            }
            return
        }

        atualizarProcessamento(conviteId, true)
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if (aceitar) {
                        compartilhamentoRepository.aceitarConvite(conviteId, usuarioId)
                    } else {
                        compartilhamentoRepository.rejeitarConvite(conviteId, usuarioId)
                    }

                    CompartilhamentoNotificationSynchronizer.getInstance(appContext)
                        .completeInvite(conviteId)

                    repository.logNotification(
                        title = appContext.getString(R.string.share_notification_history_title),
                        message = if (aceitar) {
                            appContext.getString(R.string.share_notification_history_accept)
                        } else {
                            appContext.getString(R.string.share_notification_history_reject)
                        },
                        timestampMillis = System.currentTimeMillis()
                    )
                }

                CompartilhamentoNotificationSynchronizer.triggerImmediate(appContext)
                CompartilhamentoNotificationSynchronizer.broadcastRefresh(appContext)

                _mensagens.emit(
                    if (aceitar) {
                        appContext.getString(R.string.share_notification_history_accept)
                    } else {
                        appContext.getString(R.string.share_notification_history_reject)
                    }
                )
            } catch (exception: Exception) {
                _mensagens.emit(
                    exception.message ?: appContext.getString(R.string.share_notification_action_error)
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
            .atZone(ZoneId.systemDefault())
        return dateFormatter.format(zonedDateTime)
    }
}