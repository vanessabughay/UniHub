package com.example.unihub.ui.Calendario

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Avaliacao
import com.example.unihub.data.repository.AvaliacaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class CalendarioViewModel(
    private val repository: AvaliacaoRepository,
    initialMonth: YearMonth = YearMonth.now()
) : ViewModel() {

    private val mesSelecionado = MutableStateFlow(initialMonth)
    private val visualizacao = MutableStateFlow(VisualizacaoCalendario.GRID)
    private val forceRefreshTick = MutableStateFlow(0)

    private val tituloFormatter = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale("pt", "BR"))

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private val avaliacoesFlow = forceRefreshTick.flatMapLatest {
        repository.getAvaliacao()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    val uiState: StateFlow<CalendarioUiState> =
        combine(mesSelecionado, avaliacoesFlow, visualizacao) { mes, avaliacoes, viz ->
            val tituloFormatado = mes.format(tituloFormatter)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

            val avsDoMes = avaliacoes.mapNotNull { a ->
                val data = parseToLocalDate(a.dataEntrega)
                if (data != null && data.year == mes.year && data.month == mes.month) {
                    a to data
                } else null
            }.sortedBy { it.second }


            val diasGrid = montarGridCompleto(mes, avsDoMes.map { it.first })

            CalendarioUiState(
                titulo = tituloFormatado,
                diasGrid = diasGrid,
                avaliacoesDoMes = avsDoMes.map { it.first },
                visualizacao = viz,
                isLoading = false
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
}