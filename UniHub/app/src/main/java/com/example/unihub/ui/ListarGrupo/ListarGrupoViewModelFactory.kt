package com.example.unihub.ui.ListarGrupo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.ApiDisciplinaBackend
import com.example.unihub.data.repository.ApiGrupoBackend
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.data.repository.GrupoRepository
import com.example.unihub.data.repository.Grupobackend
import com.example.unihub.ui.ListarDisciplinas.ListarDisciplinasViewModel


object ListarGrupoViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = GrupoRepository(ApiGrupoBackend())
        return ListarGrupoViewModel(repository) as T
    }
}

