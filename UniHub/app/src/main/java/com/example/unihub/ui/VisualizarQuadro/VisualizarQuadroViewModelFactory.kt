package com.example.unihub.ui.VisualizarQuadro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.QuadroRepository
import com.example.unihub.data.repository.TarefaRepository

class VisualizarQuadroViewModelFactory(
    private val quadroRepository: QuadroRepository,
    private val tarefaRepository: TarefaRepository
) : ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisualizarQuadroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VisualizarQuadroViewModel(quadroRepository, tarefaRepository) as T
        }
        throw IllegalArgumentException("Classe de ViewModel desconhecida")
    }
}