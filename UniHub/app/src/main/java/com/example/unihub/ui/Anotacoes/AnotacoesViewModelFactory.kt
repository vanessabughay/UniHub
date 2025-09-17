package com.example.unihub.ui.Anotacoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.AnotacoesRepository

class AnotacoesViewModelFactory(
    private val disciplinaId: Long,
    private val repository: AnotacoesRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AnotacoesViewModel(repository, disciplinaId) as T

}