package com.example.unihub.ui.Calendario

import androidx.compose.ui.graphics.Color
import com.example.unihub.data.model.Avaliacao
import java.time.LocalDate

// Enum para controlar o modo de visualização
enum class VisualizacaoCalendario {
    GRID,
    LISTA
}

data class AvaliacaoChipUi(
    val id: Long,
    val titulo: String,
    val subtitulo: String,
    val color: Color
)

data class DiaUi(
    val data: LocalDate?,
    val avaliacoes: List<AvaliacaoChipUi>
)

data class CalendarioUiState(
    val titulo: String = "Calendário",
    val diasGrid: List<DiaUi> = emptyList(),
    val avaliacoesDoMes: List<Avaliacao> = emptyList(),
    val visualizacao: VisualizacaoCalendario = VisualizacaoCalendario.GRID,
    val isLoading: Boolean = false,
    val error: String? = null,
    val calendarLinked: Boolean = false,
    val calendarRequiresReauth: Boolean = false,
    val calendarLastSyncedLabel: String? = null,
    val isCalendarLinking: Boolean = false,
    val isCalendarSyncing: Boolean = false,
    val calendarMessage: String? = null,
    val calendarError: String? = null
)