package com.example.unihub.ui.ManterDisciplina

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.repository.DisciplinaRepository
import kotlinx.coroutines.launch

class ManterDisciplinaViewModel(
    private val repository: DisciplinaRepository
) : ViewModel() {

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun adicionarDisciplina(disciplina: Disciplina) {
        viewModelScope.launch {
            repository.addDisciplina(disciplina)
        }
    }
}