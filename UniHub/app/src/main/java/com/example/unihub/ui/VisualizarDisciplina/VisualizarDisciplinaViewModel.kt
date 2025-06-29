package com.example.unihub.ui.VisualizarDisciplina

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.repository.DisciplinaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VisualizarDisciplinaViewModel(
    private val repository: DisciplinaRepository
) : ViewModel() {

    private val _disciplina = MutableStateFlow<Disciplina?>(null)
    val disciplina: StateFlow<Disciplina?> = _disciplina

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadDisciplina(id: String) {
        viewModelScope.launch {
            try {
                val idLong = id.toLongOrNull()
                if (idLong != null) {
                    repository.getDisciplinaById(idLong).collect {
                        _disciplina.value = it
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
