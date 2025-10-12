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
import kotlinx.coroutines.launch
import com.example.unihub.ui.Shared.NotaCampo
import com.example.unihub.ui.Shared.PesoCampo

class ManterInstituicaoViewModel(
    private val repository: InstituicaoRepository
) : ViewModel() {

    var nomeInstituicao by mutableStateOf("")
    var media by mutableStateOf("")
        private set
    var frequencia by mutableStateOf("")
        private set
    var sugestoes by mutableStateOf(listOf<Instituicao>())
    var mostrarCadastrar by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    var instituicaoId: Long? = null

    init {
        viewModelScope.launch {
            repository.instituicaoUsuario()?.let { instit ->
                instituicaoId = instit.id
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
                val lista = runCatching { repository.buscarInstituicoes(text) }.getOrDefault(emptyList())
                sugestoes = lista.distinctBy { Triple(it.nome, it.mediaAprovacao, it.frequenciaMinima) }
                mostrarCadastrar = lista.isEmpty()
            }
        }
    }

    fun onInstituicaoSelecionada(inst: Instituicao) {
        nomeInstituicao = inst.nome
        media = NotaCampo.fromDouble(inst.mediaAprovacao)
        frequencia = PesoCampo.fromDouble(inst.frequenciaMinima.toDouble())
        sugestoes = emptyList()
        mostrarCadastrar = false
    }

    fun onMediaChange(text: String) {
        val normalizado = text.replace('.', ',')
        media = NotaCampo.sanitize(normalizado)
    }

    fun onFrequenciaChange(text: String) {
        frequencia = PesoCampo.sanitize(text)
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun salvar(onSaved: () -> Unit) {
        viewModelScope.launch {
            try {
                val inst = Instituicao(
                    id = instituicaoId,
                    nome = nomeInstituicao,
                    mediaAprovacao = NotaCampo.toDouble(media) ?: 0.0,
                    frequenciaMinima = PesoCampo.toDouble(frequencia)?.toInt() ?: 0
                )
                repository.salvarInstituicao(inst)
                instituicaoId = repository.instituicaoUsuario()?.id
                errorMessage = null
                onSaved()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Erro ao salvar instituição"
            }
        }
    }
}