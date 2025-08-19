package com.example.unihub.ui.ManterConta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.InstituicaoRepository

class ManterContaViewModelFactory(
    private val repository: InstituicaoRepository = InstituicaoRepository()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManterContaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManterContaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}