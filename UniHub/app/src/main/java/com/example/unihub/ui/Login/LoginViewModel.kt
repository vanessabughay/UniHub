package com.example.unihub.ui.Login

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.unihub.data.repository.AuthRepository
import android.util.Patterns
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.content.Context

open class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    open var email by mutableStateOf("")
    open var password by mutableStateOf("")
    open var isLoading by mutableStateOf(false)
    open var errorMessage by mutableStateOf<String?>(null)
    open var success by mutableStateOf(false)
    open var hasInstitution by mutableStateOf<Boolean?>(null)

    open fun loginUser(context: Context) {
        errorMessage = null
        success = false
        hasInstitution = null

        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        if (cleanEmail.isEmpty() || cleanPassword.isEmpty()) {
            errorMessage = "Preencha todos os campos."
            return
        }

        // Usando a validação padrão do Android para e-mail
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            errorMessage = "E-mail inválido."
            return
        }

        if (cleanPassword.length < 6) {
            errorMessage = "Senha muito curta."
            return
        }

        isLoading = true

        // O 'viewModelScope' garante que a coroutine será cancelada
        // automaticamente quando o ViewModel for destruído.
        viewModelScope.launch {
            repository.loginUser(
                context = context,
                email = cleanEmail,
                password = cleanPassword,
                onSuccess = { possuiInstituicao ->
                    isLoading = false
                    hasInstitution = possuiInstituicao
                    success = true
                },
                onError = { error ->
                    isLoading = false
                    errorMessage = error
                }
            )
        }
    }

    open fun loginWithGoogle(context: Context, idToken: String) {
        errorMessage = null
        success = false
        isLoading = true
        hasInstitution = null

        viewModelScope.launch {
            repository.loginWithGoogle(
                context = context,
                idToken = idToken,
                onSuccess = { possuiInstituicao ->
                    isLoading = false
                    hasInstitution = possuiInstituicao
                    success = true
                },
                onError = { error ->
                    isLoading = false
                    errorMessage = error
                }
            )
        }
    }
}