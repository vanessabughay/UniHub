package com.example.unihub.ui.ManterAusencia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Ausencia
import com.example.unihub.data.model.Categoria
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.repository.AusenciaRepository
import com.example.unihub.data.repository.CategoriaRepository
import com.example.unihub.data.repository.DisciplinaRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ManterAusenciaViewModel(
    private val ausenciaRepository: AusenciaRepository,
    private val disciplinaRepository: DisciplinaRepository,
    private val categoriaRepository: CategoriaRepository
) : ViewModel() {
    private val _sucesso = MutableStateFlow(false)
    val sucesso: StateFlow<Boolean> = _sucesso

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _disciplina = MutableStateFlow<Disciplina?>(null)
    val disciplina: StateFlow<Disciplina?> = _disciplina

    private val _categorias = MutableStateFlow<List<Categoria>>(emptyList())
    val categorias: StateFlow<List<Categoria>> = _categorias

    private val _ausencia = MutableStateFlow<Ausencia?>(null)
    val ausencia: StateFlow<Ausencia?> = _ausencia

    fun criarAusencia(ausencia: Ausencia) {
        viewModelScope.launch {
            _erro.value = null
            try {
                ausenciaRepository.addAusencia(ausencia)
                _sucesso.value = true
            } catch (e: Exception) {
                _erro.value = e.message
            }
        }
    }

    fun atualizarAusencia(ausencia: Ausencia) {
        viewModelScope.launch {
            _erro.value = null
            try {
                val result = ausenciaRepository.updateAusencia(ausencia)
                _sucesso.value = result
            } catch (e: Exception) {
                _erro.value = e.message
            }
        }
    }

    fun loadAusencia(id: String) {
        val longId = id.toLongOrNull() ?: return
        viewModelScope.launch {
            _erro.value = null
            try {
                ausenciaRepository.getAusenciaById(longId).collect {
                    _ausencia.value = it
                }
            } catch (e: Exception) {
                _erro.value = e.message
            }
        }
    }

    fun deleteAusencia(id: Long) {
        viewModelScope.launch {
            _erro.value = null
            try {
                val result = ausenciaRepository.deleteAusencia(id)
                _sucesso.value = result
            } catch (e: Exception) {
                _erro.value = e.message
            }
        }
    }

    fun loadDisciplina(id: String) {
        val longId = id.toLongOrNull() ?: return
        viewModelScope.launch {
            _erro.value = null
            try {
                disciplinaRepository.getDisciplinaById(longId).collect {
                    _disciplina.value = it
                }
            } catch (e: Exception) {
                _erro.value = e.message
            }
        }
    }


    fun loadCategorias() {
        viewModelScope.launch {
            _erro.value = null
            try {
                categoriaRepository.listCategorias().collect { cats ->
                    _categorias.value = cats
                }
            } catch (e: Exception) {
                _erro.value = e.message
            }
        }
    }

    fun addCategoria(nomeDaNovaCategoria: String) {
        viewModelScope.launch {
            _erro.value = null
            try {
                categoriaRepository.addCategoria(nomeDaNovaCategoria)
                loadCategorias()
            } catch (e: Exception) {
                _erro.value = e.message
            }
        }
    }
    fun limparErro() {
        _erro.value = null
    }
}