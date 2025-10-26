package com.example.unihub.ui.ListarDisciplinas

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.CompartilharDisciplinaRequest
import com.example.unihub.data.repository.CompartilhamentoRepository
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.data.model.HorarioAula
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class DisciplinaResumoUi(
    val id: String,
    val nome: String,
    val horariosAulas: List<HorarioAula>
)

data class ContatoUi(
    val id: Long,
    val nome: String,
    val email: String
)

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class ListarDisciplinasViewModel(
    private val repository: DisciplinaRepository,
    private val compartilhamentoRepository: CompartilhamentoRepository
) : ViewModel() {

    private val _disciplinas = MutableStateFlow<List<DisciplinaResumoUi>>(emptyList())
    val disciplinas: StateFlow<List<DisciplinaResumoUi>> = _disciplinas.asStateFlow()

    private val _contatos = MutableStateFlow<List<ContatoUi>>(emptyList())
    val contatos: StateFlow<List<ContatoUi>> = _contatos.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _shareMessage = MutableStateFlow<String?>(null)
    val shareMessage: StateFlow<String?> = _shareMessage.asStateFlow()

    private val usuarioAtualId = 1L

    init {
        loadDisciplinas()
        loadContatos()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadDisciplinas() {
        viewModelScope.launch {
            repository.getDisciplinasResumo()
                .catch { e ->
                    _errorMessage.value = "Erro ao carregar disciplinas: ${e.message}"
                }
                .collect { disciplinasRaw ->
                    val uiDisciplinas = disciplinasRaw.map { disciplina ->
                        DisciplinaResumoUi(
                            id = disciplina.id,
                            nome = disciplina.nome,
                            horariosAulas = disciplina.aulas
                        )
                    }
                    _disciplinas.value = uiDisciplinas
                    _errorMessage.value = null
                }
        }
    }

    fun loadContatos() {
        viewModelScope.launch {
            try {
                val contatosRemotos = compartilhamentoRepository.listarContatos(usuarioAtualId)
                _contatos.value = contatosRemotos.map { ContatoUi(it.id, it.nome, it.email) }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar contatos: ${e.message}"
            }
        }
    }

    fun compartilharDisciplina(disciplinaId: Long, contatoId: Long) {
        viewModelScope.launch {
            try {
                compartilhamentoRepository.compartilharDisciplina(
                    CompartilharDisciplinaRequest(
                        disciplinaId = disciplinaId,
                        remetenteId = usuarioAtualId,
                        destinatarioId = contatoId
                    )
                )
                _shareMessage.value = "Convite enviado com sucesso"
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao compartilhar disciplina: ${e.message}"
            }
        }
    }

    fun consumirShareMessage() {
        _shareMessage.value = null
    }

    fun obterUsuarioAtualId(): Long = usuarioAtualId
}