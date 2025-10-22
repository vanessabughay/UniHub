package com.example.unihub.ui.Notificacoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object NotificacoesViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificacoesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificacoesViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}