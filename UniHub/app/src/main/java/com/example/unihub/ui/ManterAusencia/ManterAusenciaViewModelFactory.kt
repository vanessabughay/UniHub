package com.example.unihub.ui.ManterAusencia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.AusenciaRepository

class ManterAusenciaViewModelFactory(
    private val repository: AusenciaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManterAusenciaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManterAusenciaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}