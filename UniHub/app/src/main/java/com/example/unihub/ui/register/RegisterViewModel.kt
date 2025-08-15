package com.example.unihub.ui.register

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.example.unihub.data.repository.AuthRepository

class RegisterViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var success by mutableStateOf(false)

    fun registerUser() {
        errorMessage = null
        success = false

        val cleanName = name.trim()
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        if (cleanName.isEmpty() || cleanEmail.isEmpty() || cleanPassword.isEmpty()) {
            errorMessage = "Preencha todos os campos."
            return
        }

        if (!cleanName.matches(Regex("^[A-Za-zÀ-ÿ\\s]+\$"))) {
            errorMessage = "O nome deve conter apenas letras."
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            errorMessage = "E-mail inválido."
            return
        }

        if (cleanPassword.length < 6) {
            errorMessage = "A senha deve ter pelo menos 6 caracteres."
            return
        }

        isLoading = true

        repository.registerUser(
            name = cleanName,
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