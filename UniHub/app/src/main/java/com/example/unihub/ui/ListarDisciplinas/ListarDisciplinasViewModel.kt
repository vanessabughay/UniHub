package com.example.unihub.ui.ListarDisciplinas

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.repository.DisciplinaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class ListarDisciplinasViewModel(
    private val repository: DisciplinaRepository
) : ViewModel() {

    private val _disciplinas = MutableStateFlow<List<DisciplinaResumoUi>>(emptyList())
    val disciplinas: StateFlow<List<DisciplinaResumoUi>> = _disciplinas.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadDisciplinas()
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
                            disciplinaId = disciplina.disciplinaId.toString(),
                            codigo = disciplina.codigo,
                            nome = disciplina.nome,
                            horariosAulas = disciplina.aulas
                        )
                    }
                    _disciplinas.value = uiDisciplinas
                    _errorMessage.value = null
                }
        }
    }
}
