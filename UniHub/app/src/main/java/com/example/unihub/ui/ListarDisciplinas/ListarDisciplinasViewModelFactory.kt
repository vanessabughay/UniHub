package com.example.unihub.ui.ListarDisciplinas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.data.repository.FakeDisciplinaBackend

/**
 * Provides [ListarDisciplinasViewModel] instances with a simple in-memory backend.
 */
object ListarDisciplinasViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = DisciplinaRepository(FakeDisciplinaBackend())
        return ListarDisciplinasViewModel(repository) as T
    }
}