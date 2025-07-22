package com.example.unihub.ui.VisualizarDisciplina

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.model.Ausencia
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.data.repository.AusenciaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VisualizarDisciplinaViewModel(
    private val disciplinaRepository: DisciplinaRepository,
    private val ausenciaRepository: AusenciaRepository
) : ViewModel() {

    private val _disciplina = MutableStateFlow<Disciplina?>(null)
    val disciplina: StateFlow<Disciplina?> = _disciplina

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _ausencias = MutableStateFlow<List<Ausencia>>(emptyList())
    val ausencias: StateFlow<List<Ausencia>> = _ausencias

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadDisciplina(id: String) {
        viewModelScope.launch {
            try {
                val idLong = id.toLongOrNull()
                if (idLong != null) {
                    disciplinaRepository.getDisciplinaById(idLong).collect {
                        _disciplina.value = it
                    }
                    ausenciaRepository.listAusencias().collect { list ->
                        _ausencias.value = list.filter { aus -> aus.disciplinaId == idLong }
                    }
                } else {
                    _erro.value = "ID inválido"
                }
            } catch (e: Exception) {
                _erro.value = e.message
            }
        }
    }
}
