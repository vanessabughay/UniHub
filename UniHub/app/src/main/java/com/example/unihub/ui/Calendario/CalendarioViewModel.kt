package com.example.unihub.ui.Calendario

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.model.Avaliacao
import com.example.unihub.data.repository.AvaliacaoRepository
import com.example.unihub.data.repository.GoogleCalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class CalendarioViewModel(
    private val repository: AvaliacaoRepository,
    private val calendarRepository: GoogleCalendarRepository,
    private val appContext: Context,
    initialMonth: YearMonth = YearMonth.now()
) : ViewModel() {

    private val mesSelecionado = MutableStateFlow(initialMonth)
    private val visualizacao = MutableStateFlow(VisualizacaoCalendario.GRID)
    private val forceRefreshTick = MutableStateFlow(0)
    private val calendarState = MutableStateFlow(CalendarIntegrationState(linked = TokenManager.googleCalendarLinked))

    private val tituloFormatter = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale("pt", "BR"))
    private val lastSyncFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    private val zoneId: ZoneId = ZoneId.systemDefault()

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private val avaliacoesFlow = forceRefreshTick.flatMapLatest {
        repository.getAvaliacao()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    val uiState: StateFlow<CalendarioUiState> =
        combine(mesSelecionado, avaliacoesFlow, visualizacao, calendarState) { mes, avaliacoes, viz, calendar ->
            val tituloFormatado = mes.format(tituloFormatter)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

            val avsDoMes = avaliacoes.mapNotNull { a ->
                val data = parseToLocalDate(a.dataEntrega)
                if (data != null && data.year == mes.year && data.month == mes.month) {
                    a to data
                } else null
            }.sortedBy { it.second }

            val diasGrid = montarGridCompleto(mes, avsDoMes.map { it.first })
            val lastSyncLabel = calendar.lastSyncedAt?.let { lastSyncFormatter.withZone(zoneId).format(it) }

            CalendarioUiState(
                titulo = tituloFormatado,
                diasGrid = diasGrid,
                avaliacoesDoMes = avsDoMes.map { it.first },
                visualizacao = viz,
                isLoading = false,
                calendarLinked = calendar.linked,
                calendarRequiresReauth = calendar.requiresReauth,
                calendarLastSyncedLabel = lastSyncLabel,
                isCalendarLinking = calendar.isLinking,
                isCalendarSyncing = calendar.isSyncing,
                calendarMessage = calendar.message,
                calendarError = calendar.error
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CalendarioUiState(isLoading = true)
        )

    fun irParaMesAnterior() {
        mesSelecionado.value = mesSelecionado.value.minusMonths(1)
    }

    fun irParaProximoMes() {
        mesSelecionado.value = mesSelecionado.value.plusMonths(1)
    }

    fun irParaMes(novoMes: YearMonth) {
        mesSelecionado.value = novoMes
    }

    fun alterarVisualizacao() {
        visualizacao.value = if (visualizacao.value == VisualizacaoCalendario.GRID) {
            VisualizacaoCalendario.LISTA
        } else {
            VisualizacaoCalendario.GRID
        }
    }

    fun refresh() {
        viewModelScope.launch {
            forceRefreshTick.value = forceRefreshTick.value + 1
        }
    }

    fun refreshCalendarStatus() {
        viewModelScope.launch {
            calendarState.value = calendarState.value.copy(isLinking = true, error = null, message = null)
            try {
                val status = calendarRepository.fetchStatus(appContext)
                calendarState.value = calendarState.value.copy(
                    linked = status.linked,
                    lastSyncedAt = status.lastSyncedAt,
                    requiresReauth = status.requiresReauth,
                    isLinking = false
                )
            } catch (e: Exception) {
                calendarState.value = calendarState.value.copy(
                    isLinking = false,
                    error = e.message ?: "Falha ao consultar o status do Google Agenda"
                )
            }
        }
    }

    fun linkGoogleCalendar(authCode: String) {
        viewModelScope.launch {
            calendarState.value = calendarState.value.copy(isLinking = true, error = null, message = null)
            try {
                val status = calendarRepository.link(appContext, authCode)
                calendarState.value = calendarState.value.copy(
                    linked = status.linked,
                    lastSyncedAt = status.lastSyncedAt,
                    requiresReauth = status.requiresReauth,
                    isLinking = false,
                    message = "Integração com o Google Agenda ativada."
                )
            } catch (e: Exception) {
                calendarState.value = calendarState.value.copy(
                    isLinking = false,
                    error = e.message ?: "Não foi possível vincular ao Google Agenda."
                )
            }
        }
    }

    fun unlinkGoogleCalendar() {
        viewModelScope.launch {
            calendarState.value = calendarState.value.copy(isLinking = true, error = null, message = null)
            try {
                val status = calendarRepository.unlink(appContext)
                calendarState.value = calendarState.value.copy(
                    linked = status.linked,
                    lastSyncedAt = status.lastSyncedAt,
                    requiresReauth = status.requiresReauth,
                    isLinking = false,
                    message = "Integração com o Google Agenda removida."
                )
            } catch (e: Exception) {
                calendarState.value = calendarState.value.copy(
                    isLinking = false,
                    error = e.message ?: "Não foi possível remover a integração com o Google Agenda."
                )
            }
        }
    }

    fun syncGoogleCalendar() {
        if (!calendarState.value.linked) return
        viewModelScope.launch {
            calendarState.value = calendarState.value.copy(isSyncing = true, error = null, message = null)
            try {
                val result = calendarRepository.sync(appContext)
                calendarState.value = calendarState.value.copy(
                    isSyncing = false,
                    lastSyncedAt = result.lastSyncedAt,
                    message = buildString {
                        append("Sincronização concluída: ")
                        append(result.synced)
                        append(" evento(s) enviado(s)")
                        if (result.failures > 0) {
                            append(" e ")
                            append(result.failures)
                            append(" falha(s)")
                        }
                        append('.')
                    }
                )
            } catch (e: Exception) {
                calendarState.value = calendarState.value.copy(
                    isSyncing = false,
                    error = e.message ?: "Não foi possível sincronizar com o Google Agenda."
                )
            }
        }
    }

    fun reportCalendarError(message: String) {
        calendarState.value = calendarState.value.copy(error = message)
    }

    fun clearCalendarMessages() {
        calendarState.value = calendarState.value.copy(message = null, error = null)
    }

    private fun parseToLocalDate(dataString: String?): LocalDate? {
        if (dataString.isNullOrBlank()) return null
        return try {
            LocalDate.parse(dataString.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            null
        }
    }

    private fun montarGridCompleto(mes: YearMonth, avs: List<Avaliacao>): List<DiaUi> {
        val avsDoMes: List<Pair<LocalDate, Avaliacao>> = avs.mapNotNull { a ->
            val data = parseToLocalDate(a.dataEntrega)
            if (data != null && data.year == mes.year && data.month == mes.month) data to a else null
        }

        val porDia = avsDoMes.groupBy({ it.first }, { it.second })

        val primeiro = mes.atDay(1)
        val ultimo = mes.atEndOfMonth()

        val inicio = primeiro.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val fim = ultimo.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        val dias = mutableListOf<DiaUi>()
        var cursor = inicio
        while (!cursor.isAfter(fim)) {
            val dentro = cursor.month == mes.month
            val data = if (dentro) cursor else null
            val chips = if (dentro) porDia[cursor].orEmpty().mapNotNull { it.toChipUi() } else emptyList()
            dias += DiaUi(data = data, avaliacoes = chips)

            cursor = cursor.plusDays(1)
        }
        return dias
    }

    private data class CalendarIntegrationState(
        val linked: Boolean = false,
        val lastSyncedAt: Instant? = null,
        val requiresReauth: Boolean = false,
        val isLinking: Boolean = false,
        val isSyncing: Boolean = false,
        val message: String? = null,
        val error: String? = null
    )
}