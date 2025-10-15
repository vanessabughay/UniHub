package com.example.unihub.ui.ManterDisciplina

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.data.repository.InstituicaoRepository

class ManterDisciplinaViewModelFactory(
    private val disciplinaRepository: DisciplinaRepository,
    private val instituicaoRepository: InstituicaoRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManterDisciplinaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManterDisciplinaViewModel(disciplinaRepository, instituicaoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
