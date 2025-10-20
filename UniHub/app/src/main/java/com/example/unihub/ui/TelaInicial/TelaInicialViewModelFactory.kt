package com.example.unihub.ui.TelaInicial

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.apiBackend.ApiAvaliacaoBackend
import com.example.unihub.data.repository.AvaliacaoRepository
import com.example.unihub.data.apiBackend.ApiTarefaBackend
import com.example.unihub.data.repository.TarefaRepository

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class TelaInicialViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TelaInicialViewModel::class.java)) {

            val avaliacaoRepository = AvaliacaoRepository(ApiAvaliacaoBackend())
            val tarefaRepository = TarefaRepository(ApiTarefaBackend.apiService)
            return TelaInicialViewModel(avaliacaoRepository, tarefaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}