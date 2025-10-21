package com.example.unihub.ui.ListarQuadros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.GrupoRepository
import com.example.unihub.data.repository.QuadroRepository

class ListarQuadrosViewModelFactory(
    private val quadroRepository: QuadroRepository,
    private val grupoRepository: GrupoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListarQuadrosViewModel::class.java)) {
            return ListarQuadrosViewModel(quadroRepository, grupoRepository) as T
        }
        throw IllegalArgumentException("Classe de ViewModel desconhecida")
    }
}