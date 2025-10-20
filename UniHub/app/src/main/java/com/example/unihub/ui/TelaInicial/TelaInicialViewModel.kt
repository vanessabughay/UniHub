package com.example.unihub.ui.TelaInicial

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.repository.AvaliacaoRepository
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
    val titulo: String,
    val descricao: String
)

/* ====== ViewModel ====== */
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class TelaInicialViewModel(
    private val avaliacaoRepository: AvaliacaoRepository,
    private val tarefaRepository: TarefaRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(criarEstadoInicial())
    val estado: StateFlow<EstadoTelaInicial> = _estado

    private val _eventoNavegacao = MutableSharedFlow<String>()
    val eventoNavegacao: SharedFlow<String> = _eventoNavegacao

    init {
        carregarAvaliacoesReais()
        carregarTarefasReais()
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
            try {
                val dataTarefa = LocalDate.parse("${tarefa.dataCurta}/$anoAtual", formatter)
                if (!dataTarefa.isBefore(dataAtual) && !dataTarefa.isAfter(dataLimite)) {
                    tarefa to dataTarefa
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.second }
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
                "Perfil", "Disciplinas", "Serviço de nuvem", "Calendário", "Contatos",
                "Grupos", "Quadros", "Configurar notificações", "Atividades"
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
                        val avaliacoesMapeadas = avaliacoesReais.mapNotNull { mapRealToLocal(it) }
                        _estado.update { it.copy(avaliacoes = avaliacoesMapeadas) }
                        filtrarAvaliacoesEValidarTarefas()
                    }
            } catch (e: Exception) {
                _estado.update { it.copy(avaliacoes = emptyList()) }
            }
        }
    }

    /** Converte o modelo AvaliacaoReal para o modelo Avaliacao local */
    private fun mapRealToLocal(real: AvaliacaoReal): Avaliacao? {
        val data = parseToLocalDate(real.dataEntrega) ?: return null
        val localePtBr = Locale("pt", "BR")

        return Avaliacao(
            diaSemana = data.format(DateTimeFormatter.ofPattern("EEEE", localePtBr))
                .replaceFirstChar { it.titlecase(localePtBr) },
            dataCurta = data.format(DateTimeFormatter.ofPattern("dd/MM", localePtBr)),
            titulo = real.tipoAvaliacao?.takeIf { it.isNotBlank() } ?: (real.descricao ?: "Avaliação"),
            descricao = real.disciplina?.nome ?: ""
        )
    }

    /** Converte o modelo TarefaDto para o modelo Tarefa local */
    private fun mapTarefaDtoToLocal(real: TarefaDto): Tarefa? {
        val data = parseToLocalDate(real.dataPrazo) ?: return null
        val localePtBr = Locale("pt", "BR")

        return Tarefa(
            diaSemana = data.format(DateTimeFormatter.ofPattern("EEEE", localePtBr))
                .replaceFirstChar { it.titlecase(localePtBr) },
            dataCurta = data.format(DateTimeFormatter.ofPattern("dd/MM", localePtBr)),
            titulo = real.titulo,
            descricao = real.nomeQuadro
        )
    }

    /** Converte a string de data do backend para LocalDate */
    private fun parseToLocalDate(dataString: String?): LocalDate? {
        if (dataString.isNullOrBlank()) return null
        return try {
            LocalDate.parse(dataString.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            null
        }
    }
}