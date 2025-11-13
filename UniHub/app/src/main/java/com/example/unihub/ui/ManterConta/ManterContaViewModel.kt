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
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.repository.InstituicaoRepository
import kotlinx.coroutines.launch
import com.example.unihub.data.repository.AuthRepository
import com.example.unihub.ui.Shared.NotaCampo
import com.example.unihub.ui.Shared.PesoCampo


class   ManterContaViewModel(
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

    var exibindoDialogoExclusao by mutableStateOf(false)
        private set
    var excluindoConta by mutableStateOf(false)
        private set
    var contaExcluida by mutableStateOf(false)
        private set

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
                media = NotaCampo.fromDouble(inst.mediaAprovacao)
                frequencia = PesoCampo.fromDouble(inst.frequenciaMinima.toDouble())
                instituicaoId = inst.id
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
        media = NotaCampo.fromDouble(inst.mediaAprovacao)
        frequencia = PesoCampo.fromDouble(inst.frequenciaMinima.toDouble())
        sugestoes = emptyList()
        mostrarCadastrar = false
        }

    fun abrirDialogoExclusao() {
        exibindoDialogoExclusao = true
    }

    fun fecharDialogoExclusao() {
        if (!excluindoConta) {
            exibindoDialogoExclusao = false
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun salvar() {
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
                            TokenManager.saveToken(
                                context = context,
                                value = TokenManager.token ?: "",
                                nome = nome,
                                email = email,
                                usuarioId = TokenManager.usuarioId,
                                calendarLinked = TokenManager.googleCalendarLinked,
                                hasInstitution = TokenManager.hasInstitution
                            )
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
    fun deletarConta() {
        if (excluindoConta) return
        excluindoConta = true
        viewModelScope.launch {
            authRepository.deleteAccount(
                context = context,
                onSuccess = {
                    exibindoDialogoExclusao = false
                    excluindoConta = false
                    contaExcluida = true
                },
                onError = { error ->
                    excluindoConta = false
                    errorMessage = error
                }
            )
        }
    }

    fun consumirContaExcluida() {
        contaExcluida = false
    }
}