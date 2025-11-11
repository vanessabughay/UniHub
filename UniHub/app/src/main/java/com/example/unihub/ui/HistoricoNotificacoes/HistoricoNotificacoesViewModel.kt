package com.example.unihub.ui.HistoricoNotificacoes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.NotificacaoResponse
import com.example.unihub.data.repository.CompartilhamentoRepository
import com.example.unihub.data.config.TokenManager
import com.example.unihub.notifications.CompartilhamentoNotificationSynchronizer
import com.example.unihub.R
import java.time.Instant
import java.time.LocalDateTime

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        carregarHistorico(showLoading = true)
    }

    private fun carregarHistorico(showLoading: Boolean = false) {

        viewModelScope.launch {
            carregarHistoricoInterno(showLoading)
        }
    }

    private suspend fun carregarHistoricoInterno(showLoading: Boolean = false) {
        val previousState = _uiState.value
        if (showLoading) {
            _uiState.value = previousState.copy(isLoading = true)
        }

        TokenManager.loadToken(appContext)
        val usuarioId = TokenManager.usuarioId
        if (usuarioId == null || usuarioId <= 0) {
            _uiState.value = HistoricoNotificacoesUiState(isLoading = false, notificacoes = emptyList())
            _mensagens.emit(appContext.getString(R.string.share_notification_action_session_expired))
            return
        }

        try {
            val responses = withContext(Dispatchers.IO) {
                compartilhamentoRepository.listarNotificacoes(usuarioId)
            }

            val models = responses
                .map { response -> criarUiModel(response) }
                .sortedByDescending { it.timestampMillis }
                .map { it.model }

            _uiState.value = HistoricoNotificacoesUiState(
                isLoading = false,
                notificacoes = models
            )
        } catch (exception: Exception) {
            _uiState.value = previousState.copy(isLoading = false)
            _mensagens.emit(
                exception.message ?: appContext.getString(R.string.notification_history_load_error)
            )
        }
    }

    fun aceitarConvite(conviteId: Long) {
        executarAcao(conviteId, true)
    }

    fun rejeitarConvite(conviteId: Long) {
        executarAcao(conviteId, false)
    }

    private fun executarAcao(conviteId: Long, aceitar: Boolean) {
        TokenManager.loadToken(appContext)

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


                }

                CompartilhamentoNotificationSynchronizer.triggerImmediate(appContext)
                CompartilhamentoNotificationSynchronizer.broadcastRefresh(appContext)
                carregarHistoricoInterno()

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
    private fun criarUiModel(response: NotificacaoResponse): UiModelWithOrder {
        val timestamp = parseTimestamp(response.criadaEm)
        val title = response.titulo?.takeIf { it.isNotBlank() }
            ?: appContext.getString(R.string.notification_history_default_title)

        val referenceId = response.referenciaId ?: response.conviteId
        val pendingAction = response.interacaoPendente || (
                referenceId != null &&
                        !response.lida &&
                        response.tipo.equals(TIPO_CONVITE, ignoreCase = true)
                )

        return UiModelWithOrder(
            timestamp,
            HistoricoNotificacaoUiModel(
                id = response.id,
                titulo = title,
                descricao = response.mensagem,
                dataHora = formatTimestamp(timestamp),
                referenceId = referenceId,
                hasPendingInteraction = pendingAction,
                tipo = response.tipo,
                categoria = response.categoria
            )
        )
    }

    private fun parseTimestamp(raw: String?): Long {
        if (raw.isNullOrBlank()) {
            return System.currentTimeMillis()
        }

        return try {
            val localDateTime = LocalDateTime.parse(raw, DateTimeFormatter.ISO_DATE_TIME)
            localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (firstError: DateTimeParseException) {
            try {
                Instant.parse(raw).toEpochMilli()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    private data class UiModelWithOrder(
        val timestampMillis: Long,
        val model: HistoricoNotificacaoUiModel
    )

    companion object {
        private const val TIPO_CONVITE = "DISCIPLINA_COMPARTILHAMENTO"
    }
}