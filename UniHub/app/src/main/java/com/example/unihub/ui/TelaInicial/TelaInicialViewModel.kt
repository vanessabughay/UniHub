package com.example.unihub.ui.TelaInicial

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.repository.AvaliacaoRepository
import com.example.unihub.data.repository.NotificationHistoryRepository
import com.example.unihub.data.repository.TarefaRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.unihub.data.model.Avaliacao as AvaliacaoReal
import com.example.unihub.data.dto.TarefaDto

/* Modelos locais */
data class Usuario(val nome: String)

data class Avaliacao(
    val diaSemana: String,
    val dataCurta: String,
    val horaCurta: String?,
    val titulo: String,
    val descricao: String
)

data class EstadoTelaInicial(
    val usuario: Usuario = Usuario(nome = ""),
    val menuAberto: Boolean = false,
    val avaliacoes: List<Avaliacao> = emptyList(),
    val tarefas: List<Tarefa> = emptyList(),
    val opcoesMenu: List<String> = emptyList(),
    val atalhosRapidos: List<String> = emptyList(),
    val secaoAvaliacoesAberta: Boolean = true,
    val secaoTarefasAberta: Boolean = true
)

data class Tarefa(
    val diaSemana: String,
    val dataCurta: String,
    val horaCurta: String?,
    val titulo: String,
    val descricao: String,
    val prazoIso: String? = null,
    val nomeQuadro: String? = null,
    val receberNotificacoes: Boolean = true
)

/* ====== ViewModel ====== */
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class TelaInicialViewModel(
    private val avaliacaoRepository: AvaliacaoRepository,
    private val tarefaRepository: TarefaRepository,
    private val notificationHistoryRepository: NotificationHistoryRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(criarEstadoInicial())
    val estado: StateFlow<EstadoTelaInicial> = _estado

    private val _avaliacoesDetalhadas = MutableStateFlow<List<AvaliacaoReal>>(emptyList())
    val avaliacoesDetalhadas: StateFlow<List<AvaliacaoReal>> = _avaliacoesDetalhadas

    private val _eventoNavegacao = MutableSharedFlow<String>()
    val eventoNavegacao: SharedFlow<String> = _eventoNavegacao

    private val _temNotificacoesPendentes = MutableStateFlow(false)
    val temNotificacoesPendentes: StateFlow<Boolean> = _temNotificacoesPendentes


    init {
        carregarAvaliacoesReais()
        carregarTarefasReais()
        observarHistoricoNotificacoes()
    }

    private fun observarHistoricoNotificacoes() {
        viewModelScope.launch {
            notificationHistoryRepository.historyFlow.collect { entries ->
                val possuiPendencias = entries.any { it.hasPendingInteraction }
                _temNotificacoesPendentes.value = possuiPendencias
            }
        }
    }

    private fun carregarTarefasReais() {
        viewModelScope.launch {
            try {
                val tarefasReais = tarefaRepository.getProximasTarefas()
                val tarefasMapeadas = tarefasReais.mapNotNull { mapTarefaDtoToLocal(it) }
                _estado.update { it.copy(tarefas = tarefasMapeadas) }
                filtrarAvaliacoesEValidarTarefas()
            } catch (e: Exception) {
                _estado.update { it.copy(tarefas = emptyList()) }
            }
        }
    }


    /** Força o recarregamento dos dados. */
    fun refreshData() {
        carregarAvaliacoesReais()
        carregarTarefasReais()
    }



    fun abrirMenu() = _estado.update { it.copy(menuAberto = true) }
    fun fecharMenu() = _estado.update { it.copy(menuAberto = false) }
    fun alternarMenu() = _estado.update { it.copy(menuAberto = !it.menuAberto) }

    fun aoClicarOpcaoMenu(rotulo: String) {
        viewModelScope.launch {
            _eventoNavegacao.emit(rotulo)
        }
    }

    fun aoClicarAtalho(rotulo: String) {
        viewModelScope.launch {
            _eventoNavegacao.emit(rotulo)
        }
    }

    fun abrirHistoricoNotificacoes() {
        viewModelScope.launch {
            _eventoNavegacao.emit("historico_notificacoes")
        }
    }


    fun filtrarAvaliacoesEValidarTarefas() {
        val dataAtual = LocalDate.now()
        val dataLimite = dataAtual.plusDays(15)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val anoAtual = dataAtual.year.toString()

        val avaliacoesFiltradas = _estado.value.avaliacoes.mapNotNull { avaliacao ->
            try {
                val dataAvaliacao = LocalDate.parse("${avaliacao.dataCurta}/$anoAtual", formatter)
                if (!dataAvaliacao.isBefore(dataAtual) && !dataAvaliacao.isAfter(dataLimite)) {
                    avaliacao to dataAvaliacao
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.second }
            .map { it.first }

        val tarefasFiltradas = _estado.value.tarefas.mapNotNull { tarefa ->
            val dataTarefa = tarefa.prazoIso?.let { parseToLocalDate(it) }
                ?: run {
                    runCatching { LocalDate.parse("${tarefa.dataCurta}/$anoAtual", formatter) }
                        .getOrNull()
                }

            dataTarefa?.takeIf { !it.isBefore(dataAtual) && !it.isAfter(dataLimite) }
                ?.let { tarefa to it }
        }
            .sortedBy { it.second }
            .map { it.first }

        _estado.update {
            it.copy(
                avaliacoes = avaliacoesFiltradas,
                tarefas = tarefasFiltradas
            )
        }
    }

    fun alternarSecaoAvaliacoes() {
        _estado.update { it.copy(secaoAvaliacoesAberta = !it.secaoAvaliacoesAberta) }
    }

    fun alternarSecaoTarefas() {
        _estado.update { it.copy(secaoTarefasAberta = !it.secaoTarefasAberta) }
    }

    fun atualizarNomeUsuario() {
        _estado.update { it.copy(usuario = Usuario(nome = TokenManager.nomeUsuario ?: "")) }
    }

    private fun criarEstadoInicial(): EstadoTelaInicial {
        return EstadoTelaInicial(
            usuario = Usuario(nome = TokenManager.nomeUsuario ?: ""),
            menuAberto = false,
            avaliacoes = emptyList(),
            tarefas = emptyList(),
            opcoesMenu = listOf(
                "Perfil", "Disciplinas", "Calendário", "Contatos",
                "Grupos", "Quadros", "Gerenciar notificações"
            ),
            atalhosRapidos = listOf("Quadros", "Calendário", "Disciplinas", "Avaliações")
        )
    }

    /** Carrega as avaliacoes reais do repositório */
    private fun carregarAvaliacoesReais() {
        viewModelScope.launch {
            try {
                avaliacaoRepository.getAvaliacao()
                    .collect { avaliacoesReais ->
                        _avaliacoesDetalhadas.value = avaliacoesReais

                        val avaliacoesMapeadas = avaliacoesReais.mapNotNull { mapRealToLocal(it) }
                        _estado.update { it.copy(avaliacoes = avaliacoesMapeadas) }
                        filtrarAvaliacoesEValidarTarefas()
                    }
            } catch (e: Exception) {
                _estado.update { it.copy(avaliacoes = emptyList()) }
                _avaliacoesDetalhadas.value = emptyList()

            }
        }
    }

    /** Converte o modelo AvaliacaoReal para o modelo Avaliacao local */
    private fun mapRealToLocal(real: AvaliacaoReal): Avaliacao? {
        val rawDataEntrega = real.dataEntrega
        val zonedDateTime = parseDeadline(rawDataEntrega)
        val data = zonedDateTime?.toLocalDate()
            ?: parseToLocalDate(rawDataEntrega)
            ?: return null
        val localePtBr = Locale("pt", "BR")
        val possuiHorarioInformado = rawDataEntrega?.let { it.contains(":") && (it.contains("T") || it.contains(" ")) } == true
        val horaCurta = zonedDateTime?.toLocalTime()?.let {
            if (possuiHorarioInformado) {
                it.format(DateTimeFormatter.ofPattern("HH:mm", localePtBr))
            } else {
                null
            }
        }


        return Avaliacao(
            diaSemana = data.format(DateTimeFormatter.ofPattern("EEEE", localePtBr))
                .replaceFirstChar { it.titlecase(localePtBr) },
            dataCurta = data.format(DateTimeFormatter.ofPattern("dd/MM", localePtBr)),
            horaCurta = horaCurta,
            titulo = real.tipoAvaliacao?.takeIf { it.isNotBlank() } ?: (real.descricao ?: "Avaliação"),
            descricao = real.disciplina?.nome ?: ""
        )
    }

    /** Converte o modelo TarefaDto para o modelo Tarefa local */
    private fun mapTarefaDtoToLocal(real: TarefaDto): Tarefa? {
        val rawPrazo = real.dataPrazo?.takeIf { it.isNotBlank() } ?: run {
            Log.w(PRAZO_LOG_TAG, "Tarefa descartada: dataPrazo ausente (titulo=${real.titulo})")
            return null
        }
        Log.i(PRAZO_LOG_TAG, "mapTarefaDtoToLocal: recebido dataPrazo=$rawPrazo (titulo=${real.titulo})")

        val zonedDateTime = parseDeadline(rawPrazo) ?: run {
            Log.w(
                PRAZO_LOG_TAG,
                "Tarefa descartada: parseDeadline falhou para dataPrazo=$rawPrazo (titulo=${real.titulo})"
            )
            return null
        }
        val data = zonedDateTime.toLocalDate()
        val localePtBr = Locale("pt", "BR")
        val nomeQuadro = real.nomeQuadro
            .takeIf { it.isNotBlank() }
            ?: ""

        val prazoIso = zonedDateTime.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        Log.i(
            PRAZO_LOG_TAG,
            "mapTarefaDtoToLocal: prazoIso gerado=$prazoIso (titulo=${real.titulo}, quadro=${nomeQuadro})"
        )

        val possuiHorarioInformado = rawPrazo.contains(":")
        val horaCurta = zonedDateTime.toLocalTime().let { localTime ->
            if (possuiHorarioInformado) {
                localTime.format(DateTimeFormatter.ofPattern("HH:mm", localePtBr))
            } else {
                null
            }
        }

        return Tarefa(
            diaSemana = data.format(DateTimeFormatter.ofPattern("EEEE", localePtBr))
                .replaceFirstChar { it.titlecase(localePtBr) },
            dataCurta = data.format(DateTimeFormatter.ofPattern("dd/MM", localePtBr)),
            horaCurta = horaCurta,
            titulo = real.titulo,
            descricao = nomeQuadro,
            prazoIso = prazoIso,
            nomeQuadro = nomeQuadro,
            receberNotificacoes = real.receberNotificacoes
        )
    }

    /** Converte a string de data do backend para LocalDate */
    private fun parseToLocalDate(dataString: String?): LocalDate? {
        if (dataString.isNullOrBlank()) return null
        val trimmed = dataString.trim()
        val dateTimeFormatters = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        )

        dateTimeFormatters.forEach { formatter ->
            runCatching { java.time.LocalDateTime.parse(trimmed, formatter) }
                .getOrNull()
                ?.let { return it.toLocalDate() }
        }
        return runCatching {
            LocalDate.parse(trimmed.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
        }.getOrNull()
    }
    private fun parseDeadline(
        value: String?,
        zoneId: java.time.ZoneId = java.time.ZoneId.systemDefault()
    ): java.time.ZonedDateTime? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isEmpty()) return null

        runCatching { java.time.Instant.parse(trimmed) }.getOrNull()?.let {
            return it.atZone(zoneId)
        }

        runCatching { java.time.ZonedDateTime.parse(trimmed) }.getOrNull()?.let { return it }
        runCatching { java.time.OffsetDateTime.parse(trimmed) }.getOrNull()?.let {
            return it.atZoneSameInstant(zoneId)
        }

        val localDateTimeFormatters = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        )

        localDateTimeFormatters.forEach { formatter ->
            runCatching { java.time.LocalDateTime.parse(trimmed, formatter) }
                .getOrNull()
                ?.let { return it.atZone(zoneId) }
        }

        runCatching { LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE) }
            .getOrNull()
            ?.let { return it.atStartOfDay(zoneId) }

        return null
    }

    companion object {
        private const val TAG = "TelaInicialViewModel"
        private const val PRAZO_LOG_TAG = "UniHubPrazo"
    }

}