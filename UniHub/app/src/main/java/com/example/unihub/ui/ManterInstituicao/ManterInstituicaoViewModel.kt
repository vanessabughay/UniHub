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
    var possuiInstituicaoCadastrada by mutableStateOf(false)
        private set
    var carregamentoInicialConcluido by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            try {
                val instituicao = repository.instituicaoUsuario()
                instituicaoId = instituicao?.id
                possuiInstituicaoCadastrada = instituicao != null
            } catch (_: Exception) {
                possuiInstituicaoCadastrada = false
            } finally {
                carregamentoInicialConcluido = true
            }
        }
    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun onNomeInstituicaoChange(text: String) {
        nomeInstituicao = text
        errorMessage = null
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
        errorMessage = null
    }

    fun onMediaChange(text: String) {
        val normalizado = text.replace('.', ',')
        media = NotaCampo.sanitize(normalizado)
        errorMessage = null
    }

    fun onFrequenciaChange(text: String) {
        frequencia = PesoCampo.sanitize(text)
        errorMessage = null
    }

    fun isFormularioValido(): Boolean {
        val mediaValor = NotaCampo.toDouble(media)
        val frequenciaValor = PesoCampo.toDouble(frequencia)
        return nomeInstituicao.isNotBlank() && mediaValor != null && frequenciaValor != null
    }

    fun isFormularioVazio(): Boolean {
        return nomeInstituicao.isBlank() && media.isBlank() && frequencia.isBlank()
    }



    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun salvar(onSaved: () -> Unit) {
        viewModelScope.launch {
            try {
                val mediaValor = NotaCampo.toDouble(media)
                val frequenciaValor = PesoCampo.toDouble(frequencia)

                if (nomeInstituicao.isBlank() || mediaValor == null || frequenciaValor == null) {
                    errorMessage = "Preencha todos os campos obrigatórios."
                    return@launch
                }
                val inst = Instituicao(
                    id = instituicaoId,
                    nome = nomeInstituicao,
                    mediaAprovacao = mediaValor,
                    frequenciaMinima = frequenciaValor.toInt()
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
