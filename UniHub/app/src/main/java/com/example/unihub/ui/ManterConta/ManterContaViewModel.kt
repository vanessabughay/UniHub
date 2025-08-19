package com.example.unihub.ui.ManterConta

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unihub.data.model.Instituicao
import com.example.unihub.data.repository.InstituicaoRepository

class ManterContaViewModel(
    private val repository: InstituicaoRepository
) : ViewModel() {

    var nome by mutableStateOf("Victória Isabelle")
    var email by mutableStateOf("Victória.sabelle@ufpr.br")
    var senha by mutableStateOf("************")

    var nomeInstituicao by mutableStateOf("")
    var media by mutableStateOf("")
    var frequencia by mutableStateOf("")

    var sugestoes by mutableStateOf(listOf<Instituicao>())
    var mostrarCadastrar by mutableStateOf(false)

    fun onNomeInstituicaoChange(text: String) {
        nomeInstituicao = text
        sugestoes = if (text.isBlank()) emptyList() else repository.buscarInstituicoes(text)
        mostrarCadastrar = text.isNotBlank() && sugestoes.isEmpty()
    }

    fun onInstituicaoSelecionada(inst: Instituicao) {
        nomeInstituicao = inst.nome
        media = inst.mediaAprovacao.toString()
        frequencia = inst.frequenciaMinima.toString()
        sugestoes = emptyList()
        mostrarCadastrar = false
    }

    fun salvar() {
        val inst = repository.getInstituicaoPorNome(nomeInstituicao)
            ?: Instituicao(
                id = 0,
                nome = nomeInstituicao,
                mediaAprovacao = media.toDoubleOrNull() ?: 0.0,
                frequenciaMinima = frequencia.toIntOrNull() ?: 0
            )
        repository.salvarInstituicao(inst)
    }
}