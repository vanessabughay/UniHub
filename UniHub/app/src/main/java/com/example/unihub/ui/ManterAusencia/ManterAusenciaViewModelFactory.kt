package com.example.unihub.ui.ManterAusencia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.AusenciaRepository
import com.example.unihub.data.repository.DisciplinaRepository

class ManterAusenciaViewModelFactory(
    private val ausenciaRepository: AusenciaRepository,
    private val disciplinaRepository: DisciplinaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManterAusenciaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManterAusenciaViewModel(ausenciaRepository, disciplinaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}