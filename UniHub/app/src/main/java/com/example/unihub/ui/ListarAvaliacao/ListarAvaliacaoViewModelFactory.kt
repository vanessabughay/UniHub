package com.example.unihub.ui.ListarAvaliacao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.apiBackend.ApiAvaliacaoBackend
import com.example.unihub.data.repository.AvaliacaoRepository


object ListarAvaliacaoViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = AvaliacaoRepository(ApiAvaliacaoBackend())
        return ListarAvaliacaoViewModel(repository) as T
    }
}