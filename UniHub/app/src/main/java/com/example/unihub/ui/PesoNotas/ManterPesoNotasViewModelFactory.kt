package com.example.unihub.ui.PesoNotas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.apiBackend.ApiAvaliacaoBackend
import com.example.unihub.data.repository.AvaliacaoRepository
import android.content.Context
import com.example.unihub.data.repository.InstituicaoRepositoryProvider


    class ManterPesoNotasViewModelFactory(
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val avaliacaoRepo = AvaliacaoRepository(ApiAvaliacaoBackend())
            val instituicaoRepo = InstituicaoRepositoryProvider.getRepository(context)
            return ManterPesoNotasViewModel(avaliacaoRepo, instituicaoRepo) as T
        }
    }


