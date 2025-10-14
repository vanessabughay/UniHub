package com.example.unihub.ui.ManterTarefa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.TarefaRepository

class ManterTarefaViewModelFactory(
    private val repository: TarefaRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TarefaFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TarefaFormViewModel(repository) as T
        }
        throw IllegalArgumentException("Classe de ViewModel desconhecida")
    }
}