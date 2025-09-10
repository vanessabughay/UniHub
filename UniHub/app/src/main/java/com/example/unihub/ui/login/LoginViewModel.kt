package com.example.unihub.ui.login

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

    open fun loginUser(context: Context) {
        errorMessage = null
        success = false

        val cleanEmail = email.trim()
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
                onSuccess = {
                    isLoading = false
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