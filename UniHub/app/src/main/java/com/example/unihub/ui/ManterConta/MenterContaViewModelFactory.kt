package com.example.unihub.ui.ManterConta

import androidx.lifecycle.ViewModel
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.InstituicaoRepository
import com.example.unihub.data.repository.InstituicaoRepositoryProvider


class ManterContaViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManterContaViewModel::class.java)) {
            val repository = InstituicaoRepositoryProvider.getRepository(context)
            @Suppress("UNCHECKED_CAST")
            return ManterContaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}