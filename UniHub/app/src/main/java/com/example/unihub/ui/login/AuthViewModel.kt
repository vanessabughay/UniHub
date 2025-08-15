package com.example.unihub.ui.login

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.unihub.data.repository.AuthRepository
import android.util.Patterns

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var success by mutableStateOf(false)

    fun loginUser() {
        errorMessage = null
        success = false

        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        if (cleanEmail.isEmpty() || cleanPassword.isEmpty()) {
            errorMessage = "Preencha todos os campos."
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            errorMessage = "E-mail inválido."
            return
        }

        if (cleanPassword.length < 6) {
            errorMessage = "Senha muito curta."
            return
        }

        isLoading = true

        repository.loginUser(
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

private fun AuthRepository.loginUser(
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onError: Any
) {
}
