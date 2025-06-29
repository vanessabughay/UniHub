package com.example.unihub.ui.ListarDisciplinas

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.data.model.HorarioAula
import com.example.unihub.data.remote.DisciplinaApiService
import com.example.unihub.data.remote.RetrofitClient
import com.example.unihub.data.remote.DisciplinaResumo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

data class DisciplinaResumoUi(
    val id: String,
    val nome: String,
    val horariosAulas: List<HorarioAula>
)

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class ListarDisciplinasViewModel(
    private val repository: DisciplinaRepository = DisciplinaRepository(RetrofitClient.disciplinaService)
) : ViewModel() {

    private val _disciplinas = MutableStateFlow<List<DisciplinaResumoUi>>(emptyList())
    val disciplinas: StateFlow<List<DisciplinaResumoUi>> = _disciplinas.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadDisciplinas()
    }

    fun loadDisciplinas() {
        viewModelScope.launch {
            repository.getDisciplinasResumo()
                .onStart { _isLoading.value = true }
                .catch { e ->
                    _errorMessage.value = "Erro ao carregar disciplinas: ${e.message ?: "Erro desconhecido"}"
                    _isLoading.value = false
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
                    _isLoading.value = false
                }
        }
    }
}