package com.example.unihub.ui.Registro

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.repository.AuthRepository
import android.util.Patterns
import kotlinx.coroutines.launch

open class RegistroViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    open var name by mutableStateOf("")
    open var email by mutableStateOf("")
    open var password by mutableStateOf("")
    open var confirmPassword by mutableStateOf("")
    open var isLoading by mutableStateOf(false)
    open var errorMessage by mutableStateOf<String?>(null)
    open var success by mutableStateOf(false)

    open fun registerUser() {
        errorMessage = null
        success = false

        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()
        val cleanConfirmPassword = confirmPassword.trim()

        if (cleanName.isEmpty() || cleanEmail.isEmpty() || cleanPassword.isEmpty() || cleanConfirmPassword.isEmpty()) {
            errorMessage = "Preencha todos os campos."
            return
        }

        if (cleanPassword != cleanConfirmPassword) {
            errorMessage = "As senhas não coincidem."
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

        // Usa viewModelScope para iniciar a coroutine
        viewModelScope.launch {
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
}