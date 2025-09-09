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
import com.example.unihub.data.repository.AuthRepository


class ManterContaViewModel(
    private val repository: InstituicaoRepository,
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {

    var nome by mutableStateOf("")
    var email by mutableStateOf("")
    var senha by mutableStateOf("")
    var confirmarSenha by mutableStateOf("")

    var nomeInstituicao by mutableStateOf("")
    var media by mutableStateOf("")
    var frequencia by mutableStateOf("")

    var sugestoes by mutableStateOf(listOf<Instituicao>())
    var mostrarCadastrar by mutableStateOf(false)

    var success by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private var nomeOriginal = ""
    private var emailOriginal = ""
    private var senhaOriginal = ""
    private var instituicaoId: Long? = null

    init {
        TokenManager.loadToken(context)
        nomeOriginal = TokenManager.nomeUsuario ?: ""
        emailOriginal = TokenManager.emailUsuario ?: ""
        nome = nomeOriginal
        email = emailOriginal
        carregarInstituicaoUsuario()
    }

    fun carregarInstituicaoUsuario() {
        viewModelScope.launch {
            repository.instituicaoUsuario()?.let { inst ->
                nomeInstituicao = inst.nome
                media = inst.mediaAprovacao.toString()
                frequencia = inst.frequenciaMinima.toString()
                instituicaoId = inst.id
            }
        }
    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun onNomeInstituicaoChange(text: String) {
        nomeInstituicao = text
        instituicaoId = null
        if (text.isBlank()) {
            sugestoes = emptyList()
            mostrarCadastrar = false
        } else {
            viewModelScope.launch {
                try {
                    val lista = repository.buscarInstituicoes(text)
                    sugestoes = lista.distinctBy { it.nome }
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
        instituicaoId = null
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun salvar() {
        viewModelScope.launch {
            try {
                val inst = Instituicao(
                    id = instituicaoId,
                    nome = nomeInstituicao,
                    mediaAprovacao = media.toDoubleOrNull() ?: 0.0,
                    frequenciaMinima = frequencia.toIntOrNull() ?: 0
                )
                repository.salvarInstituicao(inst)
                instituicaoId = repository.instituicaoUsuario()?.id

                val nomeAlterado = nome != nomeOriginal
                val emailAlterado = email != emailOriginal
                val senhaAlterada = senha.isNotBlank() || confirmarSenha.isNotBlank()

                if (senhaAlterada) {
                    val cleanSenha = senha.trim()
                    val cleanConfirmar = confirmarSenha.trim()
                    if (cleanSenha.isEmpty() || cleanConfirmar.isEmpty()) {
                        errorMessage = "Preencha todos os campos de senha."
                        return@launch
                    }
                    if (cleanSenha != cleanConfirmar) {
                        errorMessage = "As senhas não coincidem."
                        return@launch
                    }
                    if (cleanSenha.length < 6) {
                        errorMessage = "A senha deve ter pelo menos 6 caracteres."
                        return@launch
                    }
                    if (cleanSenha == senhaOriginal) {
                        errorMessage = "A nova senha deve ser diferente da senha anterior."
                        return@launch
                    }
                }

                if (nomeAlterado || emailAlterado || senhaAlterada) {
                    authRepository.updateUser(
                        context,
                        nome,
                        email,
                        if (senhaAlterada) senha.trim() else null,
                        onSuccess = {
                            TokenManager.saveToken(context, TokenManager.token ?: "", nome, email)
                            nomeOriginal = nome
                            emailOriginal = email
                            if (senhaAlterada) {
                                senhaOriginal = senha.trim()
                                senha = ""
                                confirmarSenha = ""
                            }
                            success = true
                        },
                        onError = { error ->
                            errorMessage = error
                        }
                    )
                } else {
                    success = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.message
            }
        }
    }
}