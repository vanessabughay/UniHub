package com.example.unihub.ui.VisualizarDisciplina

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.DisciplinaRepository

class VisualizarDisciplinaViewModelFactory(
    private val repository: DisciplinaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisualizarDisciplinaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VisualizarDisciplinaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
