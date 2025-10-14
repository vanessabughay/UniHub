package com.example.unihub.ui.ManterTarefa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.GrupoRepository
import com.example.unihub.data.repository.QuadroRepository
import com.example.unihub.data.repository.TarefaRepository

class ManterTarefaViewModelFactory(
    private val repository: TarefaRepository,
    private val quadroRepository: QuadroRepository,
    private val grupoRepository: GrupoRepository,
    private val contatoRepository: ContatoRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TarefaFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TarefaFormViewModel(
                repository = repository,
                quadroRepository = quadroRepository,
                grupoRepository = grupoRepository,
                contatoRepository = contatoRepository
            ) as T
        }
        throw IllegalArgumentException("Classe de ViewModel desconhecida")
    }
}