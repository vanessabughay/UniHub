package com.example.unihub.ui.ListarDisciplinas

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.ApiDisciplinaBackend
import com.example.unihub.data.repository.DisciplinaRepository

object ListarDisciplinasViewModelFactory : ViewModelProvider.Factory {
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListarDisciplinasViewModel::class.java)) {
            val repository = DisciplinaRepository(ApiDisciplinaBackend())
            return ListarDisciplinasViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
