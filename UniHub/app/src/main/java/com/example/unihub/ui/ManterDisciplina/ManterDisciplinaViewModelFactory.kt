package com.example.unihub.ui.ManterDisciplina

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.DisciplinaRepository

class ManterDisciplinaViewModelFactory(
    private val repository: DisciplinaRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManterDisciplinaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManterDisciplinaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
