package com.example.unihub.ui.HistoricoNotificacoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.repository.NotificationHistoryRepository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class HistoricoNotificacaoUiModel(
    val id: Long,
    val titulo: String,
    val descricao: String,
    val dataHora: String
)

data class HistoricoNotificacoesUiState(
    val isLoading: Boolean = false,
    val notificacoes: List<HistoricoNotificacaoUiModel> = emptyList()
)

class HistoricoNotificacoesViewModel(
    private val repository: NotificationHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoricoNotificacoesUiState(isLoading = true))
    val uiState: StateFlow<HistoricoNotificacoesUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern(
        "dd/MM/yyyy 'às' HH:mm",
        Locale("pt", "BR")
    )

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
                        dataHora = formatTimestamp(entry.timestampMillis)
                    )
                }

                _uiState.value = HistoricoNotificacoesUiState(
                    isLoading = false,
                    notificacoes = models
                )
            }
        }
    }

    private fun formatTimestamp(timestampMillis: Long): String {
        val zonedDateTime = Instant.ofEpochMilli(timestampMillis)
            .atZone(ZoneId.systemDefault())
        return dateFormatter.format(zonedDateTime)
    }
}