package com.example.unihub.ui.ManterQuadro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.repository.QuadroRepository
import com.example.unihub.data.model.Quadro
import com.example.unihub.data.model.Estado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed class FormResult {
    object Idle : FormResult()
    object Success : FormResult()
    data class Error(val message: String) : FormResult()
}

class QuadroFormViewModel(
    private val repository: QuadroRepository
) : ViewModel() {

    private val _quadroState = MutableStateFlow<Quadro?>(null)
    val quadro: StateFlow<Quadro?> = _quadroState.asStateFlow()

    private val _formResult = MutableStateFlow<FormResult>(FormResult.Idle)
    val formResult: StateFlow<FormResult> = _formResult.asStateFlow()

    fun carregarQuadro(quadroId: String) {
        viewModelScope.launch {
            _quadroState.value = repository.getQuadroById(quadroId)
        }
    }

    fun salvarOuAtualizarQuadro(quadro: Quadro) {
        viewModelScope.launch {
            try {
                var quadroToSave = quadro
                val existingQuadro = quadro.id?.takeIf { it.isNotBlank() }?.let { repository.getQuadroById(it) }


                if (existingQuadro != null) {
                    quadroToSave = quadroToSave.copy(
                        dataInicio = existingQuadro.dataInicio,
                        donoId = existingQuadro.donoId
                    )
                }

                // Ajusta a data de fim automaticamente apenas quando necessário
                if (quadroToSave.estado == Estado.INATIVO && existingQuadro?.estado != Estado.INATIVO && quadroToSave.dataFim == null) {
                    quadroToSave = quadroToSave.copy(dataFim = System.currentTimeMillis())
                } else if (quadroToSave.estado != Estado.INATIVO && existingQuadro?.estado == Estado.INATIVO && quadroToSave.dataFim == existingQuadro.dataFim) {
                    quadroToSave = quadroToSave.copy(dataFim = null)
                }

                if (!quadroToSave.id.isNullOrBlank()) {
                    repository.updateQuadro(quadroToSave)
                } else {
                    repository.addQuadro(quadroToSave)

                }
                _formResult.value = FormResult.Success
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    401, 403 -> "Sua sessão expirou. Faça login novamente para continuar."
                    else -> e.message()
                } ?: "Erro do servidor ao salvar/atualizar quadro."
                _formResult.value = FormResult.Error(message)
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
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    401, 403 -> "Sua sessão expirou. Faça login novamente para continuar."
                    else -> e.message()
                } ?: "Erro do servidor ao excluir quadro."
                _formResult.value = FormResult.Error(message)
            } catch (e: Exception) {
                _formResult.value = FormResult.Error(e.message ?: "Erro ao excluir")
            }
        }
    }

    fun resetFormResult() {
        _formResult.value = FormResult.Idle
    }
}