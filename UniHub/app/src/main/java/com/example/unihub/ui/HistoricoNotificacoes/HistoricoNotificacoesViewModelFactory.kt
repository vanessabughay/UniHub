package com.example.unihub.ui.HistoricoNotificacoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.NotificationHistoryRepository

class HistoricoNotificacoesViewModelFactory(
    private val repository: NotificationHistoryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoricoNotificacoesViewModel::class.java)) {
            return HistoricoNotificacoesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}