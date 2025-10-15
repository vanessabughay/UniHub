package com.example.unihub.ui.Anotacoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Anotacao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.example.unihub.data.repository.AnotacoesRepository
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.System

class AnotacoesViewModel(
    private val repository: AnotacoesRepository = AnotacoesRepository(),
    private val disciplinaId: Long
) : ViewModel() {

    private val _anotacoes = MutableStateFlow<List<Anotacao>>(emptyList())
    val anotacoes: StateFlow<List<Anotacao>> = _anotacoes.asStateFlow()

    init {
        carregarAnotacoes()
    }

    private fun carregarAnotacoes() {
        viewModelScope.launch {
            try {
                repository.listar(disciplinaId).collect { lista ->
                    _anotacoes.value = lista
                }
            } catch (e: Exception) {
                // Lógica para tratar erros de carregamento
            }
        }
    }

    fun alternarExpandida(id: Long) = _anotacoes.update { lista ->
        lista.map { a ->
            if (a.id == id)
                a.copy(
                    expandida = !a.expandida,
                    rascunhoTitulo = a.titulo,
                    rascunhoConteudo = a.conteudo
                )
            else a
        }
    }

    fun alterarRascunhoTitulo(id: Long, valor: String) = _anotacoes.update { lista ->
        lista.map { if (it.id == id) it.copy(rascunhoTitulo = valor) else it }
    }

    fun alterarRascunhoConteudo(id: Long, valor: String) = _anotacoes.update { lista ->
        lista.map { if (it.id == id) it.copy(rascunhoConteudo = valor) else it }
    }

    fun salvar(id: Long) {
        viewModelScope.launch {
            val anotacao = _anotacoes.value.find { it.id == id } ?: return@launch
            try {
                val resultado = if (id < 0) { // IDs temporários (negativos) são novas anotações
                    repository.criar(disciplinaId, anotacao.rascunhoTitulo, anotacao.rascunhoConteudo)
                } else {
                    repository.atualizar(disciplinaId, id, anotacao.rascunhoTitulo, anotacao.rascunhoConteudo)
                }
                // Atualiza a lista com o objeto retornado da API
                _anotacoes.update { lista ->
                    lista.map { a ->
                        if (a.id == id) {
                            resultado.copy(expandida = false, rascunhoTitulo = resultado.titulo, rascunhoConteudo = resultado.conteudo)
                        } else {
                            a
                        }
                    }
                }
            } catch (e: Exception) {
                // Tratar erro
            }
        }
    }

    fun cancelar(id: Long) = _anotacoes.update { lista ->
        val anotacao = lista.find { it.id == id }
        if (anotacao != null && anotacao.id < 0) { // Se for nova anotação, remove
            lista.filterNot { it.id == id }
        } else { // Se for existente, apenas colapsa
            lista.map { a ->
                if (a.id == id)
                    a.copy(expandida = false, rascunhoTitulo = a.titulo, rascunhoConteudo = a.conteudo)
                else a
            }
        }
    }

    fun excluir(id: Long) {
        if (id < 0) {
            _anotacoes.update { it.filterNot { a -> a.id == id } }
            return
        }
        viewModelScope.launch {
            try {
                repository.excluir(disciplinaId, id)
                // Atualiza a lista local para uma resposta mais rápida na UI
                _anotacoes.update { it.filterNot { a -> a.id == id } }
            } catch (e: Exception) {
                // Tratar erro
            }
        }
    }

    fun novaAnotacao() {
        // Usa um ID temporário e negativo para identificar uma nova anotação
        val novaAnotacao = Anotacao(
            id = -(System.currentTimeMillis()),
            titulo = "",
            conteudo = "",
            expandida = true
        )
        _anotacoes.update { listOf(novaAnotacao) + it }
    }
}