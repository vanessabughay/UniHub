package com.example.unihub.ui.ManterQuadro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.data.repository.GrupoRepository
import com.example.unihub.data.repository.QuadroRepository

class QuadroFormViewModelFactory(
    private val quadroRepository: QuadroRepository,
    private val disciplinaRepository: DisciplinaRepository,
    private val contatoRepository: ContatoRepository,
    private val grupoRepository: GrupoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuadroFormViewModel::class.java)) {
            return QuadroFormViewModel(
                quadroRepository = quadroRepository,
                disciplinaRepository = disciplinaRepository,
                contatoRepository = contatoRepository,
                grupoRepository = grupoRepository
            ) as T
        }
        throw IllegalArgumentException("Classe de ViewModel desconhecida")
    }
}