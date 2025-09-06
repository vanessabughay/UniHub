package com.example.unihub.ui.TelaInicial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.unihub.data.api.TokenManager


/*  Modelos exemplo  */
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
class TelaInicialViewModel : ViewModel() {

    private val _estado = MutableStateFlow(criarEstadoInicial())
    val estado: StateFlow<EstadoTelaInicial> = _estado

    private val _eventoNavegacao = MutableSharedFlow<String>()
    val eventoNavegacao: SharedFlow<String> = _eventoNavegacao

    fun abrirMenu() = _estado.update { it.copy(menuAberto = true) }
    fun fecharMenu() = _estado.update { it.copy(menuAberto = false) }
    fun alternarMenu() = _estado.update { it.copy(menuAberto = !it.menuAberto) }

    /* Pontos de extensão para cliques (navegação, etc.) */
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


    // Filtrando avaliações e tarefas para os próximos 15 dias
    fun filtrarAvaliacoesEValidarTarefas() {
        val dataAtual = LocalDate.now()
        val dataLimite = dataAtual.plusDays(15)
        val anoAtual = dataAtual.year.toString()

        val avaliacoesFiltradas = _estado.value.avaliacoes.filter { avaliacao ->
            // Concatena o ano atual à string da data para análise
            val dataComAno = "${avaliacao.dataCurta}/$anoAtual"
            val dataAvaliacao = LocalDate.parse(dataComAno, DateTimeFormatter.ofPattern("dd/MM/yyyy"))

            dataAvaliacao.isBefore(dataLimite) || dataAvaliacao.isEqual(dataLimite)
        }

        val tarefasFiltradas = _estado.value.tarefas.filter { tarefa ->
            // Faça o mesmo para as tarefas
            val dataComAno = "${tarefa.dataCurta}/$anoAtual"
            val dataTarefa = LocalDate.parse(dataComAno, DateTimeFormatter.ofPattern("dd/MM/yyyy"))

            dataTarefa.isBefore(dataLimite) || dataTarefa.isEqual(dataLimite)
        }

        // Atualizando o estado com as avaliações e tarefas filtradas
        _estado.update {
            it.copy(
                avaliacoes = avaliacoesFiltradas,
                tarefas = tarefasFiltradas
            )
        }
    }

    // Alternar a visibilidade das seções (Avaliações e Tarefas)
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
            avaliacoes = listOf(
                Avaliacao(
                    diaSemana = "Quarta",
                    dataCurta = "27/03",
                    titulo = "Prova 1",
                    descricao = "Estrutura de dados"
                ),
                Avaliacao(
                    diaSemana = "Segunda",
                    dataCurta = "01/04",
                    titulo = "Trabalho Microsserviços",
                    descricao = "Desenvolvimento Web II"
                )
                ),

                tarefas = listOf(
                    Tarefa(
                        diaSemana = "Sexta",
                        dataCurta = "28/08",
                        titulo = "Fazer projeto Unihub",
                        descricao = "Implementar o menu lateral."
                    ),
                    Tarefa(
                        diaSemana = "Domingo",
                        dataCurta = "31/08",
                        titulo = "Revisar matéria",
                        descricao = "Revisão para a prova de Design de UI."
                    )

            ),
            opcoesMenu = listOf(
                "Perfil",
                "Disciplinas",
                "Serviço de nuvem",
                "Calendário",
                "Contatos",
                "Grupos",
                "Projetos",
                "Configurar notificações",
                "Atividades"
            ),
            atalhosRapidos = listOf("Projetos", "Calendário", "Disciplinas", "Avaliações")
        )
    }
}