package com.example.unihub.ui.ManterGrupo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.apiBackend.ApiGrupoBackend
import com.example.unihub.data.apiBackend.ApiContatoBackend
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.Contatobackend
import com.example.unihub.data.repository.GrupoRepository
import com.example.unihub.data.repository.Grupobackend

class ManterGrupoViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManterGrupoViewModel::class.java)) {
            val backend: Grupobackend = ApiGrupoBackend()
            val repository = GrupoRepository(backend)
            val contatoBackend: Contatobackend = ApiContatoBackend()
            val contatoRepository = ContatoRepository(contatoBackend)
            //return ManterGrupoViewModel(repository, contatoRepository) as T
            return ManterGrupoViewModel(repository, contatoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

