package com.example.unihub.ui.VisualizarQuadro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.QuadroRepository

class VisualizarQuadroViewModelFactory(
    private val quadroRepository: QuadroRepository
) : ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisualizarQuadroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VisualizarQuadroViewModel(quadroRepository) as T
        }
        throw IllegalArgumentException("Classe de ViewModel desconhecida")
    }
}