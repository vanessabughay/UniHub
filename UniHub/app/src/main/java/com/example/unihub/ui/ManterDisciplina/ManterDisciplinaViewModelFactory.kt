package com.example.unihub.ui.ManterDisciplina

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.ApiDisciplinaBackend
import com.example.unihub.data.repository.DisciplinaRepository

class ManterDisciplinaViewModelFactory(
    private val disciplinaId: Long?
) : ViewModelProvider.Factory {
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManterDisciplinaViewModel::class.java)) {
            val repository = DisciplinaRepository(ApiDisciplinaBackend()) // ou FakeBackend()
            return ManterDisciplinaViewModel(disciplinaId, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
