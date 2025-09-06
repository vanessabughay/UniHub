package com.example.unihub.ui.ManterConta

import android.os.Build
import android.content.Context
import androidx.annotation.RequiresExtension
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Instituicao
import com.example.unihub.data.api.TokenManager
import com.example.unihub.data.repository.InstituicaoRepository
import kotlinx.coroutines.launch


class ManterContaViewModel(
    private val repository: InstituicaoRepository,
    private val context: Context
) : ViewModel() {

    var nome by mutableStateOf("")
    var email by mutableStateOf("")
    var senha by mutableStateOf("")

    var nomeInstituicao by mutableStateOf("")
    var media by mutableStateOf("")
    var frequencia by mutableStateOf("")

    var sugestoes by mutableStateOf(listOf<Instituicao>())
    var mostrarCadastrar by mutableStateOf(false)

    init {
        TokenManager.loadToken(context)
        nome = TokenManager.nomeUsuario ?: ""
        email = TokenManager.emailUsuario ?: ""
        carregarInstituicaoUsuario()
    }

    fun carregarInstituicaoUsuario() {
        viewModelScope.launch {
            repository.instituicaoUsuario()?.let { inst ->
                nomeInstituicao = inst.nome
                media = inst.mediaAprovacao.toString()
                frequencia = inst.frequenciaMinima.toString()
            }
        }
    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun onNomeInstituicaoChange(text: String) {
        nomeInstituicao = text
        if (text.isBlank()) {
            sugestoes = emptyList()
            mostrarCadastrar = false
        } else {
            viewModelScope.launch {
                try {
                    val lista = repository.buscarInstituicoes(text)
                    sugestoes = lista
                    mostrarCadastrar = lista.isEmpty()
                } catch (e: Exception) {
                    sugestoes = emptyList()
                    mostrarCadastrar = true
                }
            }
        }
    }


    fun onInstituicaoSelecionada(inst: Instituicao) {
        nomeInstituicao = inst.nome
        media = inst.mediaAprovacao.toString()
        frequencia = inst.frequenciaMinima.toString()
        sugestoes = emptyList()
        mostrarCadastrar = false
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun salvar() {
        viewModelScope.launch {
            try {
                val instExistente = repository.getInstituicaoPorNome(nomeInstituicao).getOrNull()
                val inst = instExistente?.copy(
                    mediaAprovacao = media.toDoubleOrNull() ?: instExistente.mediaAprovacao,
                    frequenciaMinima = frequencia.toIntOrNull() ?: instExistente.frequenciaMinima
                ) ?: Instituicao(
                    nome = nomeInstituicao,
                    mediaAprovacao = media.toDoubleOrNull() ?: 0.0,
                    frequenciaMinima = frequencia.toIntOrNull() ?: 0
                )
                repository.salvarInstituicao(inst)
                TokenManager.saveToken(
                    context,
                    TokenManager.token ?: "",
                    nome,
                    email
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}