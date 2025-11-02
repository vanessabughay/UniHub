package com.example.unihub.ui.Notificacoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.NotificacoesRepository

class NotificacoesViewModelFactory(
    private val repository: NotificacoesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificacoesViewModel::class.java)) {
            return NotificacoesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}