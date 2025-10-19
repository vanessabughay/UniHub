package com.example.unihub.ui.Calendario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.AvaliacaoRepository

class CalendarioViewModelFactory(
    private val avaliacaoRepository: AvaliacaoRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarioViewModel::class.java)) {
            return CalendarioViewModel(repository = avaliacaoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}