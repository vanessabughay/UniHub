package com.example.unihub.ui.ListarDisciplinas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.remote.RetrofitClient
import com.example.unihub.data.repository.ApiDisciplinaBackend
import com.example.unihub.data.repository.CompartilhamentoRepository
import com.example.unihub.data.repository.DisciplinaRepository

object ListarDisciplinasViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = DisciplinaRepository(ApiDisciplinaBackend())
        val compartilhamentoRepository = CompartilhamentoRepository(RetrofitClient.compartilhamentoApiService)
        return ListarDisciplinasViewModel(repository, compartilhamentoRepository) as T
    }
}
