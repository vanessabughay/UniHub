package com.example.unihub.ui.ManterAvaliacao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.apiBackend.ApiAvaliacaoBackend
import com.example.unihub.data.apiBackend.ApiContatoBackend
import com.example.unihub.data.apiBackend.ApiDisciplinaBackend
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.Contatobackend
import com.example.unihub.data.repository.AvaliacaoRepository
import com.example.unihub.data.repository.Avaliacaobackend
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.data.repository._disciplinabackend

class ManterAvaliacaoViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManterAvaliacaoViewModel::class.java)) {
            // Backend e Repositório de Avaliação
            val backend: Avaliacaobackend = ApiAvaliacaoBackend()
            val repository = AvaliacaoRepository(backend)

            // Backend e Repositório de Contato
            val contatoBackend: Contatobackend = ApiContatoBackend()
            val contatoRepository = ContatoRepository(contatoBackend)

            // Backend e Repositório de Disciplina
            val disciplinaBackend: _disciplinabackend = ApiDisciplinaBackend()
            val disciplinaRepository = DisciplinaRepository(disciplinaBackend)

            //return ManterAvaliacaoViewModel(repository, contatoRepository) as T
            return ManterAvaliacaoViewModel(repository, contatoRepository, disciplinaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

