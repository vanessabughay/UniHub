package com.example.unihub.ui.ManterDisciplina

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.ApiDisciplinaBackend
import com.example.unihub.data.repository.DisciplinaRepository

object ManterDisciplinaViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = DisciplinaRepository(ApiDisciplinaBackend())
        return ManterDisciplinaViewModel(repository) as T
    }
}