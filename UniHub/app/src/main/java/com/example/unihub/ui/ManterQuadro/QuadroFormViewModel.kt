package com.example.unihub.ui.ManterQuadro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.repository.QuadroRepository
import com.example.unihub.data.model.QuadroDePlanejamento
import com.example.unihub.data.model.Estado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class FormResult {
    object Idle : FormResult()
    object Success : FormResult()
    data class Error(val message: String) : FormResult()
}

class QuadroFormViewModel(
    private val repository: QuadroRepository
) : ViewModel() {

    private val _quadroState = MutableStateFlow<QuadroDePlanejamento?>(null)
    val quadro: StateFlow<QuadroDePlanejamento?> = _quadroState.asStateFlow()

    private val _formResult = MutableStateFlow<FormResult>(FormResult.Idle)
    val formResult: StateFlow<FormResult> = _formResult.asStateFlow()

    fun carregarQuadro(quadroId: String) {
        viewModelScope.launch {
            _quadroState.value = repository.getQuadroById(quadroId)
        }
    }

    fun salvarOuAtualizarQuadro(quadro: QuadroDePlanejamento) {
        viewModelScope.launch {
            try {
                var quadroToSave = quadro
                val existingQuadro = if (quadro.id.isNotBlank()) {
                    repository.getQuadroById(quadro.id)
                } else {
                    null
                }

                // Lógica para definir dataFim baseada na mudança de estado
                if (quadroToSave.estado == Estado.INATIVO && existingQuadro?.estado != Estado.INATIVO) {
                    quadroToSave = quadroToSave.copy(dataFim = System.currentTimeMillis())
                } else if (quadroToSave.estado != Estado.INATIVO && existingQuadro?.estado == Estado.INATIVO) {
                    quadroToSave = quadroToSave.copy(dataFim = null)
                }

                if (quadroToSave.id.isNotBlank()) {
                    repository.updateQuadro(quadroToSave)
                } else {
                    val newQuadroWithId = quadroToSave.copy(id = UUID.randomUUID().toString())
                    repository.addQuadro(newQuadroWithId)
                }
                _formResult.value = FormResult.Success
            } catch (e: Exception) {
                _formResult.value = FormResult.Error(e.message ?: "Erro desconhecido ao salvar/atualizar quadro.")
            }
        }
    }

    fun excluirQuadro(quadroId: String) {
        viewModelScope.launch {
            try {
                repository.deleteQuadro(quadroId)
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