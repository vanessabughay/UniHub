package com.example.unihub.ui.ListarDisciplinas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.remote.RetrofitClient
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.data.repository.RealDisciplinaBackend

object ListarDisciplinasViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val backend = RealDisciplinaBackend(RetrofitClient.disciplinaApiService)
        val repository = DisciplinaRepository(backend)
        return ListarDisciplinasViewModel(repository) as T
    }
}
