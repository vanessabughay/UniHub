package com.example.unihub.ui.ManterColuna

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.repository.ColunaRepository
import com.example.unihub.data.model.Coluna
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.unihub.data.model.Status
import java.util.UUID
import androidx.lifecycle.ViewModelProvider

sealed class FormResult {
    object Idle : FormResult()
    object Success : FormResult()
    data class Error(val message: String) : FormResult()
}


class ColunaFormViewModel(
    private val repository: ColunaRepository
) : ViewModel() {

    private val _colunaState = MutableStateFlow<Coluna?>(null)
    val coluna: StateFlow<Coluna?> = _colunaState.asStateFlow()

    private val _formResult = MutableStateFlow<FormResult>(FormResult.Idle)
    val formResult: StateFlow<FormResult> = _formResult.asStateFlow()

    fun carregarColuna(colunaId: String) {
        viewModelScope.launch {
            _colunaState.value = repository.getColunaById(colunaId)
        }
    }

    fun salvarOuAtualizarColuna(coluna: Coluna) {
        viewModelScope.launch {
            try {
                val existingColuna = if (coluna.id.isNotBlank()) {
                    repository.getColunaById(coluna.id)
                } else {
                    null
                }

                var colunaToSave = coluna

                if (colunaToSave.status == Status.CONCLUIDA && existingColuna?.status != Status.CONCLUIDA) {
                    colunaToSave = colunaToSave.copy(dataFim = System.currentTimeMillis())
                } else if (colunaToSave.status != Status.CONCLUIDA && existingColuna?.status == Status.CONCLUIDA) {
                    colunaToSave = colunaToSave.copy(dataFim = null)
                }

                if (colunaToSave.id.isNotBlank()) {
                    repository.updateColuna(colunaToSave)
                } else {
                    val newColunaWithId = colunaToSave.copy(id = UUID.randomUUID().toString())
                    repository.addColuna(newColunaWithId)
                }

                _formResult.value = FormResult.Success
            } catch (e: Exception) {
                _formResult.value = FormResult.Error(e.message ?: "Erro desconhecido ao salvar/atualizar coluna.")
            }
        }
    }

    fun excluirColuna(colunaId: String) {
        viewModelScope.launch {
            try {
                repository.deleteColuna(colunaId)
                _formResult.value = FormResult.Success
            } catch (e: Exception) {
                _formResult.value = FormResult.Error(e.message ?: "Erro ao excluir")
            }
        }
    }

    fun resetFormResult() {
        _formResult.value = FormResult.Idle
    }
}