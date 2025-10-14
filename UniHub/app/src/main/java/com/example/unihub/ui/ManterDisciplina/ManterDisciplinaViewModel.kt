package com.example.unihub.ui.ManterDisciplina

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.data.repository.InstituicaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class ManterDisciplinaViewModel(
    private val repository: DisciplinaRepository,
    private val instituicaoRepository: InstituicaoRepository
) : ViewModel() {

    // Estados da disciplina
    private val _disciplina = MutableStateFlow<Disciplina?>(null)
    val disciplina: StateFlow<Disciplina?> = _disciplina

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _sucesso = MutableStateFlow<Boolean>(false)
    val sucesso: StateFlow<Boolean> = _sucesso

    private val _frequenciaMinima = MutableStateFlow<Int?>(null)
    val frequenciaMinima: StateFlow<Int?> = _frequenciaMinima

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadDisciplina(id: String) {
        val longId = id.toLongOrNull()
        if (longId != null) {
            viewModelScope.launch {
                try {
                    repository.getDisciplinaById(longId).collect {
                        _disciplina.value = it
                    }
                } catch (e: Exception) {
                    _erro.value = e.message
                }
            }
        } else {
            _erro.value = "ID inválido"
        }
    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun createDisciplina(disciplina: Disciplina) {
        viewModelScope.launch {
            try {
                repository.addDisciplina(disciplina)
                _sucesso.value = true
            } catch (e: Exception) {
                _erro.value = e.message
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun updateDisciplina(disciplina: Disciplina) {
        viewModelScope.launch {
            try {
                val result = repository.updateDisciplina(disciplina)
                _sucesso.value = result
            } catch (e: Exception) {
                _erro.value = e.message
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun deleteDisciplina(id: String) {
        viewModelScope.launch {
            try {
                val result = repository.deleteDisciplina(id)
                _sucesso.value = result
            } catch (e: Exception) {
                _erro.value = e.message
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun carregarFrequenciaMinima() {
        viewModelScope.launch {
            try {
                val instituicao = instituicaoRepository.instituicaoUsuario()
                _frequenciaMinima.value = instituicao?.frequenciaMinima
            } catch (e: Exception) {
                _erro.value = e.message
            }
        }
    }
}
