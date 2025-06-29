package com.example.unihub.ui.VisualizarDisciplina

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.model.HorarioAula
import com.example.unihub.data.remote.RetrofitClient // Para instanciar o repositório
import com.example.unihub.data.repository.DisciplinaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

// DTO para o estado da UI do formulário de Disciplina
// Usamos String? para campos de texto que podem estar vazios/nulos antes da validação
// E List<HorarioAula> para os horários, permitindo adicionar/remover dinamicamente
data class DisciplinaFormState(
    val id: String? = null, // Será nulo para novas disciplinas
    val nome: String = "",
    val professor: String = "",
    val periodo: String = "",
    val cargaHoraria: String = "", // String para facilitar a entrada do usuário
    val aulas: List<HorarioAula> = emptyList(),
    val dataInicioSemestre: LocalDate? = null, // Pode ser String para entrada, mas LocalDate é mais correto para modelo
    val dataFimSemestre: LocalDate? = null,   // Pode ser String para entrada, mas LocalDate é mais correto para modelo
    val emailProfessor: String = "",
    val plataforma: String = "",
    val telefoneProfessor: String = "",
    val salaProfessor: String = "",
    val isAtiva: Boolean = true,
    val receberNotificacoes: Boolean = true
)

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class ManterDisciplinaViewModel(
    // Injeção de dependência do repositório
    private val repository: DisciplinaRepository = DisciplinaRepository(RetrofitClient.disciplinaService)
) : ViewModel() {

    // Estado do formulário que será observado pela UI
    private val _disciplinaFormState = MutableStateFlow(DisciplinaFormState())
    val disciplinaFormState: StateFlow<DisciplinaFormState> = _disciplinaFormState.asStateFlow()

    // Estados de UI para operações assíncronas
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveSuccess = MutableStateFlow<Boolean?>(null) // true para sucesso, false para falha, null para aguardando
    val saveSuccess: StateFlow<Boolean?> = _saveSuccess.asStateFlow()

    private val _deleteSuccess = MutableStateFlow<Boolean?>(null)
    val deleteSuccess: StateFlow<Boolean?> = _deleteSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // --- Funções para atualizar os campos do formulário ---
    // Métodos para cada campo, para que a UI possa atualizar o estado
    fun onNomeChanged(nome: String) { _disciplinaFormState.value = _disciplinaFormState.value.copy(nome = nome) }
    fun onProfessorChanged(professor: String) { _disciplinaFormState.value = _disciplinaFormState.value.copy(professor = professor) }
    fun onPeriodoChanged(periodo: String) { _disciplinaFormState.value = _disciplinaFormState.value.copy(periodo = periodo) }
    fun onCargaHorariaChanged(cargaHoraria: String) { _disciplinaFormState.value = _disciplinaFormState.value.copy(cargaHoraria = cargaHoraria) }
    fun onEmailProfessorChanged(email: String) { _disciplinaFormState.value = _disciplinaFormState.value.copy(emailProfessor = email) }
    fun onPlataformaChanged(plataforma: String) { _disciplinaFormState.value = _disciplinaFormState.value.copy(plataforma = plataforma) }
    fun onTelefoneProfessorChanged(telefone: String) { _disciplinaFormState.value = _disciplinaFormState.value.copy(telefoneProfessor = telefone) }
    fun onSalaProfessorChanged(sala: String) { _disciplinaFormState.value = _disciplinaFormState.value.copy(salaProfessor = sala) }
    fun onIsAtivaChanged(isAtiva: Boolean) { _disciplinaFormState.value = _disciplinaFormState.value.copy(isAtiva = isAtiva) }
    fun onReceberNotificacoesChanged(receber: Boolean) { _disciplinaFormState.value = _disciplinaFormState.value.copy(receberNotificacoes = receber) }
    fun onDataInicioSemestreChanged(date: LocalDate) { _disciplinaFormState.value = _disciplinaFormState.value.copy(dataInicioSemestre = date) }
    fun onDataFimSemestreChanged(date: LocalDate) { _disciplinaFormState.value = _disciplinaFormState.value.copy(dataFimSemestre = date) }

    // Funções para gerenciar HorariosAula
    fun addHorarioAula(horario: HorarioAula) {
        _disciplinaFormState.value = _disciplinaFormState.value.copy(
            aulas = _disciplinaFormState.value.aulas + horario
        )
    }
    fun removeHorarioAula(horario: HorarioAula) {
        _disciplinaFormState.value = _disciplinaFormState.value.copy(
            aulas = _disciplinaFormState.value.aulas - horario
        )
    }
    // Uma função para atualizar um horário existente pode ser útil
    fun updateHorarioAula(oldHorario: HorarioAula, newHorario: HorarioAula) {
        _disciplinaFormState.value = _disciplinaFormState.value.copy(
            aulas = _disciplinaFormState.value.aulas.map { if (it == oldHorario) newHorario else it }
        )
    }


    // --- Funções para Carregar/Inicializar Disciplina ---
    fun loadDisciplina(disciplinaId: String?) {
        if (disciplinaId == null) {
            // Modo de criação: Reinicia o formulário
            _disciplinaFormState.value = DisciplinaFormState()
            return
        }

        // Modo de edição: Carrega os dados da disciplina
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.getDisciplinaById(disciplinaId).collect { disciplina ->
                    disciplina?.let {
                        _disciplinaFormState.value = DisciplinaFormState(
                            id = it.id,
                            nome = it.nome,
                            professor = it.professor,
                            periodo = it.periodo,
                            cargaHoraria = it.cargaHoraria.toString(), // Converte Int para String
                            aulas = it.aulas,
                            dataInicioSemestre = it.dataInicioSemestre,
                            dataFimSemestre = it.dataFimSemestre,
                            emailProfessor = it.emailProfessor,
                            plataforma = it.plataforma,
                            telefoneProfessor = it.telefoneProfessor,
                            salaProfessor = it.salaProfessor,
                            isAtiva = it.isAtiva,
                            receberNotificacoes = it.receberNotificacoes
                        )
                    } ?: run {
                        _errorMessage.value = "Disciplina não encontrada."
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar disciplina: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Funções para Salvar (Adicionar/Atualizar) Disciplina ---
    fun saveDisciplina() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _saveSuccess.value = null // Reseta o status de sucesso

            val currentFormState = _disciplinaFormState.value

            // Validação básica (adicione mais validações conforme necessário)
            if (currentFormState.nome.isBlank() || currentFormState.professor.isBlank()) {
                _errorMessage.value = "Nome e Professor são campos obrigatórios."
                _isLoading.value = false
                _saveSuccess.value = false
                return@launch
            }

            // Tenta converter cargaHoraria para Int
            val cargaHorariaInt = currentFormState.cargaHoraria.toIntOrNull()
            if (cargaHorariaInt == null) {
                _errorMessage.value = "Carga Horária deve ser um número válido."
                _isLoading.value = false
                _saveSuccess.value = false
                return@launch
            }


            // Converte DisciplinaFormState para Disciplina (para enviar ao repositório)
            val disciplinaToSave = Disciplina(
                id = currentFormState.id ?: "", // ID será gerado pelo backend se for novo
                nome = currentFormState.nome,
                professor = currentFormState.professor,
                periodo = currentFormState.periodo,
                cargaHoraria = cargaHorariaInt,
                aulas = currentFormState.aulas,
                dataInicioSemestre = currentFormState.dataInicioSemestre ?: LocalDate.now(), // Defina um default ou trate como erro
                dataFimSemestre = currentFormState.dataFimSemestre ?: LocalDate.now().plusMonths(4), // Defina um default ou trate como erro
                emailProfessor = currentFormState.emailProfessor,
                plataforma = currentFormState.plataforma,
                telefoneProfessor = currentFormState.telefoneProfessor,
                salaProfessor = currentFormState.salaProfessor,
                isAtiva = currentFormState.isAtiva,
                receberNotificacoes = currentFormState.receberNotificacoes
            )

            try {
                if (currentFormState.id == null) {
                    // Adicionar nova disciplina
                    repository.addDisciplina(disciplinaToSave)
                    _saveSuccess.value = true
                    _errorMessage.value = "Disciplina adicionada com sucesso!"
                    // Opcional: Limpar formulário após adição ou navegar de volta
                    _disciplinaFormState.value = DisciplinaFormState()
                } else {
                    // Atualizar disciplina existente
                    val success = repository.updateDisciplina(disciplinaToSave)
                    _saveSuccess.value = success
                    if (success) {
                        _errorMessage.value = "Disciplina atualizada com sucesso!"
                    } else {
                        _errorMessage.value = "Falha ao atualizar disciplina." // Caso o repositório retorne false
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao salvar disciplina: ${e.message}"
                _saveSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Funções para Deletar Disciplina ---
    fun deleteDisciplina(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _deleteSuccess.value = null

            try {
                val success = repository.deleteDisciplina(id)
                _deleteSuccess.value = success
                if (success) {
                    _errorMessage.value = "Disciplina deletada com sucesso!"
                    // Opcional: Navegar de volta para a tela de listagem
                } else {
                    _errorMessage.value = "Falha ao deletar disciplina."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao deletar disciplina: ${e.message}"
                _deleteSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Limpa os estados de sucesso/erro para que não apareçam novamente em recomposições
    fun clearSaveStatus() {
        _saveSuccess.value = null
    }
    fun clearDeleteStatus() {
        _deleteSuccess.value = null
    }
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}