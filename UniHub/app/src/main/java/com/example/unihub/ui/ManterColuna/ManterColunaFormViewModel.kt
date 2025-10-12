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

    //  Adiciona o quadroId como parâmetro
    fun carregarColuna(quadroId: String, colunaId: String) {
        viewModelScope.launch {
            // Passa o quadroId para o repositório
            _colunaState.value = repository.getColunaById(quadroId, colunaId)
        }
    }

    // Adiciona o quadroId como parâmetro
    fun salvarOuAtualizarColuna(quadroId: String, coluna: Coluna) {
        viewModelScope.launch {
            try {
                val existingColuna = if (coluna.id.isNotBlank()) {
                    // Passa o quadroId para o repositório
                    repository.getColunaById(quadroId, coluna.id)
                } else {
                    null
                }

                var colunaToSave = coluna


                if (colunaToSave.id.isNotBlank()) {
                    // MUDANÇA: Passa o quadroId para o repositório
                    repository.updateColuna(quadroId, colunaToSave)
                } else {
                    // MUDANÇA: Passa o quadroId para o repositório
                    val newColunaWithId = colunaToSave.copy(id = UUID.randomUUID().toString())
                    repository.addColuna(quadroId, newColunaWithId)
                }

                _formResult.value = FormResult.Success
            } catch (e: Exception) {
                _formResult.value = FormResult.Error(e.message ?: "Erro desconhecido ao salvar/atualizar coluna.")
            }
        }
    }

    //  Adiciona o quadroId como parâmetro
    fun excluirColuna(quadroId: String, colunaId: String) {
        viewModelScope.launch {
            try {
                // passa o quadroId para o repositório
                repository.deleteColuna(quadroId, colunaId)
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