package com.example.unihub.ui.ManterConta

import androidx.lifecycle.ViewModel
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.InstituicaoRepository
import com.example.unihub.data.repository.InstituicaoRepositoryProvider
import com.example.unihub.data.repository.AuthRepository


class ManterContaViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManterContaViewModel::class.java)) {
            val repository = InstituicaoRepositoryProvider.getRepository(context)
            val authRepository = AuthRepository()
            @Suppress("UNCHECKED_CAST")
            return ManterContaViewModel(repository, authRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}