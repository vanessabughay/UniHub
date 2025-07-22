package com.example.unihub.ui.VisualizarDisciplina

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.data.repository.AusenciaRepository

class VisualizarDisciplinaViewModelFactory(
    private val disciplinaRepository: DisciplinaRepository,
    private val ausenciaRepository: AusenciaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisualizarDisciplinaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VisualizarDisciplinaViewModel(disciplinaRepository, ausenciaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
