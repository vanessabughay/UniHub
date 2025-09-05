package com.example.unihub.ui.ManterGrupo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.ApiGrupoBackend
import com.example.unihub.data.repository.GrupoRepository
import com.example.unihub.data.repository.Grupobackend

class ManterGrupoViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManterGrupoViewModel::class.java)) {
            val backend: Grupobackend = ApiGrupoBackend()
            val repository = GrupoRepository(backend)
            return ManterGrupoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}