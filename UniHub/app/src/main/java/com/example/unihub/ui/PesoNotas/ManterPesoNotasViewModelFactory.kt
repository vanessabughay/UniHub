package com.example.unihub.ui.PesoNotas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.apiBackend.ApiAvaliacaoBackend
import com.example.unihub.data.repository.AvaliacaoRepository

object ManterPesoNotasViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = AvaliacaoRepository(ApiAvaliacaoBackend())
        return ManterPesoNotasViewModel(repo) as T
    }
}


