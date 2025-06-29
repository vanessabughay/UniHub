package com.example.unihub.ui.ManterDisciplina

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.model.HorarioAula
import com.example.unihub.data.repository.DisciplinaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class ManterDisciplinaViewModel(
    disciplinaId: Long?,  // recebe o id no construtor
    private val repo: DisciplinaRepository
) : ViewModel() {

    private val _disciplina = MutableStateFlow<Disciplina?>(null)
    val disciplina: StateFlow<Disciplina?> = _disciplina.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Estados para UI (estado separado para edição)
    var codigo by mutableStateOf("")
    var periodo by mutableStateOf("")
    var nome by mutableStateOf("")
    var professor by mutableStateOf("")
    var cargaHoraria by mutableStateOf("")
    var aulas by mutableStateOf<List<HorarioAula>>(emptyList())
    var dataInicioSemestre by mutableStateOf("")
    var dataFimSemestre by mutableStateOf("")
    var emailProfessor by mutableStateOf("")
    var plataforma by mutableStateOf("")
    var telefoneProfessor by mutableStateOf("")
    var salaProfessor by mutableStateOf("")
    var isAtiva by mutableStateOf(true)

    init {
        // Se id não for nulo e diferente de 0, carrega a disciplina
        if (disciplinaId != null && disciplinaId != 0L) {
            carregarDisciplina(disciplinaId)
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun carregarDisciplina(id: Long) {
        viewModelScope.launch {
            repo.getDisciplinaById(id.toString())
                .catch { /* tratar erro, opcionalmente enviar estado de erro */ }
                .collect { disc ->
                    disc?.let {
                        _disciplina.value = it
                        popularEstados(it)
                    }
                }
        }
    }

    private fun popularEstados(disciplina: Disciplina) {
        codigo = disciplina.codigo
        periodo = disciplina.periodo
        nome = disciplina.nome
        professor = disciplina.professor
        cargaHoraria = disciplina.cargaHoraria.toString()
        aulas = disciplina.aulas
        dataInicioSemestre = disciplina.dataInicioSemestre.format(dateFormatter)
        dataFimSemestre = disciplina.dataFimSemestre.format(dateFormatter)
        emailProfessor = disciplina.emailProfessor
        plataforma = disciplina.plataforma
        telefoneProfessor = disciplina.telefoneProfessor
        salaProfessor = disciplina.salaProfessor
        isAtiva = disciplina.isAtiva
    }

    fun montarDisciplinaParaSalvar(): Disciplina {
        return Disciplina(
            disciplinaId = _disciplina.value?.disciplinaId ?: 0L, // 0L para nova
            codigo = codigo,
            nome = nome,
            professor = professor,
            periodo = periodo,
            cargaHoraria = cargaHoraria.toIntOrNull() ?: 0,
            aulas = aulas,
            dataInicioSemestre = LocalDate.parse(dataInicioSemestre, dateFormatter),
            dataFimSemestre = LocalDate.parse(dataFimSemestre, dateFormatter),
            emailProfessor = emailProfessor,
            plataforma = plataforma,
            telefoneProfessor = telefoneProfessor,
            salaProfessor = salaProfessor,
            isAtiva = isAtiva,
            receberNotificacoes = false // implementar depois se quiser
        )
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun salvarDisciplina(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch {
            try {
                val disc = montarDisciplinaParaSalvar()
                if (disc.disciplinaId == 0L) {
                    repo.addDisciplina(disc)
                } else {
                    repo.updateDisciplina(disc)
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun excluirDisciplina(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch {
            try {
                val id = _disciplina.value?.disciplinaId ?: return@launch
                repo.deleteDisciplina(id.toString())
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    // Função para atualizar uma aula na lista
    fun atualizarAula(index: Int, novaAula: HorarioAula) {
        val listaMutavel = aulas.toMutableList()
        if (index in listaMutavel.indices) {
            listaMutavel[index] = novaAula
            aulas = listaMutavel
        }
    }

    // Adicionar ou remover aulas conforme qtdAulas
    fun ajustarQuantidadeAulas(qtdAulas: Int) {
        val atual = aulas.toMutableList()
        if (qtdAulas > atual.size) {
            repeat(qtdAulas - atual.size) {
                atual.add(HorarioAula(diaDaSemana = "Segunda-feira", sala = "", horarioInicio = "", horarioFim = ""))
            }
        } else if (qtdAulas < atual.size) {
            for (i in atual.size - 1 downTo qtdAulas) {
                atual.removeAt(i)
            }
        }
        aulas = atual
    }
}
