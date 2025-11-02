package com.example.unihub.ui.ManterDisciplina

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.data.repository.InstituicaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import com.example.unihub.data.model.Ausencia
import java.time.Instant
import java.time.LocalDate

data class DisciplinaFormState(
    val id: String? = null,
    val codigo: String = "",
    val nomeDisciplina: String = "",
    val nomeProfessor: String = "",
    val periodo: String = "",
    val cargaHoraria: String = "",
    val qtdSemanas: String = "",
    val qtdAulasSemana: String = "1",
    val dataInicioSemestre: Long = 0L,
    val dataFimSemestre: Long = 0L,
    val aulas: List<AulaInfo> = listOf(AulaInfo()),
    val emailProfessor: String = "",
    val plataformas: String = "",
    val telefoneProfessor: String = "",
    val salaProfessor: String = "",
    val ausenciasPermitidas: String = "",
    val ausenciasExistentes: List<Ausencia> = emptyList(),
    val isAtiva: Boolean = true,
    val receberNotificacoes: Boolean = true,
    val isLoading: Boolean = false,
    val erro: String? = null,
    val sucesso: Boolean = false,
    val frequenciaMinima: Int? = null,
    val isDataLoaded: Boolean = false
)

class ManterDisciplinaViewModel(
    private val repository: DisciplinaRepository,
    private val instituicaoRepository: InstituicaoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DisciplinaFormState())
    val uiState: StateFlow<DisciplinaFormState> = _uiState

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadDisciplina(id: String) {
        _uiState.update { it.copy(isLoading = true, id = id) }
        val longId = id.toLongOrNull()
        if (longId != null) {
            viewModelScope.launch {
                try {
                    val d = repository.getDisciplinaById(longId).first()

                    d?.let { disciplina ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                codigo = disciplina.codigo.orEmpty(),
                                nomeDisciplina = disciplina.nome.orEmpty(),
                                nomeProfessor = disciplina.professor.orEmpty(),
                                periodo = disciplina.periodo.orEmpty(),
                                cargaHoraria = (disciplina.cargaHoraria ?: 0).toString(),
                                qtdSemanas = (disciplina.qtdSemanas ?: 0).toString(),
                                qtdAulasSemana = disciplina.aulas.size.toString(),
                                aulas = disciplina.aulas.map {
                                    AulaInfo(
                                        dia = it.diaDaSemana,
                                        ensalamento = it.sala,
                                        horarioInicio = it.horarioInicio,
                                        horarioFim = it.horarioFim
                                    )
                                },
                                dataInicioSemestre = disciplina.dataInicioSemestre
                                    .atStartOfDay(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli(),
                                dataFimSemestre = disciplina.dataFimSemestre
                                    .atStartOfDay(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli(),
                                emailProfessor = disciplina.emailProfessor.orEmpty(),
                                plataformas = disciplina.plataforma.orEmpty(),
                                telefoneProfessor = disciplina.telefoneProfessor.orEmpty(),
                                salaProfessor = disciplina.salaProfessor.orEmpty(),
                                ausenciasPermitidas = disciplina.ausenciasPermitidas?.toString() ?: "",
                                ausenciasExistentes = disciplina.ausencias,
                                isAtiva = disciplina.isAtiva,
                                receberNotificacoes = disciplina.receberNotificacoes,
                                isLoading = false,
                                isDataLoaded = true
                            )
                        }
                    } ?: _uiState.update { it.copy(erro = "Disciplina não encontrada", isLoading = false) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(erro = e.message, isLoading = false) }
                }
            }
        } else {
            _uiState.update { it.copy(erro = "ID inválido", isLoading = false) }
        }
    }

    // Funções de Update de Estado - Para serem chamadas diretamente pelo Composable
    fun updateField(newValue: String, field: (DisciplinaFormState) -> String, copy: (DisciplinaFormState, String) -> DisciplinaFormState) {
        _uiState.update { state -> copy(state, newValue) }
    }

    fun updateAula(index: Int, update: (AulaInfo) -> AulaInfo) {
        _uiState.update { state ->
            val novasAulas = state.aulas.toMutableList()
            novasAulas[index] = update(novasAulas[index])
            state.copy(aulas = novasAulas)
        }
    }

    fun updateQtdAulasSemana(qtd: String) {
        _uiState.update { state ->
            val quantidade = qtd.toIntOrNull() ?: 0
            val novasAulas = if (quantidade > state.aulas.size) {
                state.aulas + List(quantidade - state.aulas.size) { AulaInfo() }
            } else if (quantidade < state.aulas.size && quantidade >= 0) {
                state.aulas.take(quantidade)
            } else {
                state.aulas
            }
            state.copy(qtdAulasSemana = qtd, aulas = novasAulas)
        }
    }

    fun setIsAtiva(novaAtiva: Boolean) {
        _uiState.update {
            Log.d("DISCIPLINA_DEBUG", "VM: isAtiva de ${it.isAtiva} para $novaAtiva")
            it.copy(isAtiva = novaAtiva)
        }
    }

    fun setDataInicioSemestre(data: Long) {
        _uiState.update { it.copy(dataInicioSemestre = data) }
    }

    fun setDataFimSemestre(data: Long) {
        _uiState.update { it.copy(dataFimSemestre = data) }
    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun saveDisciplina() {
        _uiState.update { it.copy(isLoading = true, erro = null) }
        val state = _uiState.value

        Log.d("DISCIPLINA_DEBUG", "Salvando disciplina com isAtiva = ${state.isAtiva}")

        val inicio = Instant.ofEpochMilli(state.dataInicioSemestre)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val fim = Instant.ofEpochMilli(state.dataFimSemestre)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val disciplinaToSave = Disciplina(
            id = state.id?.toLongOrNull(),
            codigo = state.codigo,
            nome = state.nomeDisciplina,
            professor = state.nomeProfessor,
            periodo = state.periodo,
            cargaHoraria = state.cargaHoraria.toIntOrNull() ?: 0,
            qtdSemanas = state.qtdSemanas.toIntOrNull() ?: 0,
            dataInicioSemestre = inicio,
            dataFimSemestre = fim,
            emailProfessor = state.emailProfessor,
            plataforma = state.plataformas,
            telefoneProfessor = state.telefoneProfessor,
            salaProfessor = state.salaProfessor,
            ausencias = state.ausenciasExistentes,
            ausenciasPermitidas = state.ausenciasPermitidas.toIntOrNull(),
            isAtiva = state.isAtiva,
            receberNotificacoes = state.receberNotificacoes,
            avaliacoes = emptyList(), // Assumindo lista vazia ou que Avaliações são gerenciadas em outro lugar
            aulas = state.aulas.map {
                com.example.unihub.data.model.HorarioAula(
                    diaDaSemana = it.dia,
                    sala = it.ensalamento,
                    horarioInicio = it.horarioInicio,
                    horarioFim = it.horarioFim
                )
            }

        )

        Log.i("DISCIPLINA_SENT", "Objeto Disciplina enviado: ${disciplinaToSave.toString()}")

        viewModelScope.launch {
            try {
                if (state.id == null) {
                    repository.addDisciplina(disciplinaToSave)
                    _uiState.update { it.copy(sucesso = true, isLoading = false) }
                } else {
                    val result = repository.updateDisciplina(disciplinaToSave)
                    _uiState.update { it.copy(sucesso = result, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.message, isLoading = false) }
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun deleteDisciplina(id: String) {
        _uiState.update { it.copy(isLoading = true, erro = null) }
        viewModelScope.launch {
            try {
                val result = repository.deleteDisciplina(id)
                _uiState.update { it.copy(sucesso = result, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.message, isLoading = false) }
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun carregarFrequenciaMinima() {
        viewModelScope.launch {
            try {
                val instituicao = instituicaoRepository.instituicaoUsuario()
                _uiState.update { it.copy(frequenciaMinima = instituicao?.frequenciaMinima) }
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.message) }
            }
        }
    }
}