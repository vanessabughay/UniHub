package com.example.unihub.ui.ManterInstituicao

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Instituicao
import com.example.unihub.data.repository.InstituicaoRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ManterInstituicaoViewModel(
    private val repository: InstituicaoRepository
) : ViewModel() {

    var nomeInstituicao by mutableStateOf("")
    var media by mutableStateOf("")
    var frequencia by mutableStateOf("")

    var sugestoes by mutableStateOf(listOf<Instituicao>())
    var mostrarCadastrar by mutableStateOf(false)

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun onNomeInstituicaoChange(text: String) {
        nomeInstituicao = text
        if (text.isBlank()) {
            sugestoes = emptyList()
            mostrarCadastrar = false
        } else {
            viewModelScope.launch {
                val lista = runCatching { repository.buscarInstituicoes(text) }.getOrDefault(emptyList())
                sugestoes = lista
                mostrarCadastrar = lista.isEmpty()
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
    fun salvar(onSaved: () -> Unit) {
        viewModelScope.launch {
            val inst = repository.getInstituicaoPorNome(nomeInstituicao)
                .firstOrNull()
                ?: Instituicao(
                    id = 0,
                    nome = nomeInstituicao,
                    mediaAprovacao = media.toDoubleOrNull() ?: 0.0,
                    frequenciaMinima = frequencia.toIntOrNull() ?: 0
                )
            repository.salvarInstituicao(inst)
            onSaved()
        }
    }
}