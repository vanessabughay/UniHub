package com.example.unihub.ui.ManterInstituicao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import com.example.unihub.data.repository.InstituicaoRepository
import com.example.unihub.data.repository.InstituicaoRepositoryProvider

class ManterInstituicaoViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManterInstituicaoViewModel::class.java)) {
            val repository = InstituicaoRepositoryProvider.getRepository(context)
            @Suppress("UNCHECKED_CAST")
            return ManterInstituicaoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}