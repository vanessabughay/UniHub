package com.example.unihub.ui.ListarContato

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.apiBackend.ApiContatoBackend
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.Contatobackend


object ListarContatoViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val backend: Contatobackend = ApiContatoBackend()
        val repository = ContatoRepository(backend)

        if (modelClass.isAssignableFrom(ListarContatoViewModel::class.java)) {
            return ListarContatoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
