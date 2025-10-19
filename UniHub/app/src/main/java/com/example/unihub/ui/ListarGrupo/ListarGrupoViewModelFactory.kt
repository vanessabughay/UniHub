package com.example.unihub.ui.ListarGrupo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.apiBackend.ApiContatoBackend
import com.example.unihub.data.apiBackend.ApiGrupoBackend
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.GrupoRepository


object ListarGrupoViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = GrupoRepository(ApiGrupoBackend())
        val contatoRepository = ContatoRepository(ApiContatoBackend())
        return ListarGrupoViewModel(repository, contatoRepository) as T
    }
}

