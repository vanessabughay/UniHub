package com.example.unihub.ui.ManterAusencia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Ausencia
import com.example.unihub.data.repository.AusenciaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ManterAusenciaViewModel(private val repository: AusenciaRepository) : ViewModel() {
    private val _sucesso = MutableStateFlow(false)
    val sucesso: StateFlow<Boolean> = _sucesso

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    fun criarAusencia(ausencia: Ausencia) {
        viewModelScope.launch {
            try {
                repository.addAusencia(ausencia)
                _sucesso.value = true
            } catch (e: Exception) {
                _erro.value = e.message
            }
        }
    }
}