package com.example.unihub.ui.ListarDisciplinas

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.apiBackend.ApiDisciplinaBackend
import com.example.unihub.data.repository.DisciplinaRepository

/**
 * Provides ListarDisciplinasViewModel instances with a simple in-memory backend.
 */
object ListarDisciplinasViewModelFactory : ViewModelProvider.Factory {
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = DisciplinaRepository(ApiDisciplinaBackend())
        return ListarDisciplinasViewModel(repository) as T
    }
}
