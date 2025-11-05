package com.example.unihub.ui.HistoricoNotificacoes

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.apiBackend.ApiCompartilhamentoBackend
import com.example.unihub.data.repository.CompartilhamentoRepository

class HistoricoNotificacoesViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoricoNotificacoesViewModel::class.java)) {
            val compartilhamentoRepository = CompartilhamentoRepository(ApiCompartilhamentoBackend())
            return HistoricoNotificacoesViewModel(
                application,
                compartilhamentoRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}