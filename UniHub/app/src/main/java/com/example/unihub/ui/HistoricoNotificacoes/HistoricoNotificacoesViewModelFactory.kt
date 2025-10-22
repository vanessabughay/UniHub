package com.example.unihub.ui.HistoricoNotificacoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object HistoricoNotificacoesViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoricoNotificacoesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoricoNotificacoesViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}