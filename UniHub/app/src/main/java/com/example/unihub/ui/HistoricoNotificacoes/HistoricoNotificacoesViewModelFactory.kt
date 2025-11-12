package com.example.unihub.ui.HistoricoNotificacoes

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.apiBackend.ApiCompartilhamentoBackend
import com.example.unihub.data.apiBackend.ApiContatoBackend
import com.example.unihub.data.repository.CompartilhamentoRepository
import com.example.unihub.data.repository.ContatoRepository

class HistoricoNotificacoesViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoricoNotificacoesViewModel::class.java)) {
            val compartilhamentoRepository = CompartilhamentoRepository(ApiCompartilhamentoBackend())
            val contatoRepository = ContatoRepository(ApiContatoBackend())
            return HistoricoNotificacoesViewModel(
                application,
                compartilhamentoRepository,
                contatoRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}