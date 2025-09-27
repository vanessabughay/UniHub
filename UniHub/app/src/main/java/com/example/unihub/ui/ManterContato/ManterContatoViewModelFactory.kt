package com.example.unihub.ui.ManterContato

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.apiBackend.ApiContatoBackend
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.Contatobackend

class ManterContatoViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManterContatoViewModel::class.java)) {
            val backend: Contatobackend = ApiContatoBackend()
            val repository = ContatoRepository(backend)
            return ManterContatoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}