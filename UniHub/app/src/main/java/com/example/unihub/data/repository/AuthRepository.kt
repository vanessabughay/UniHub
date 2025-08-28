package com.example.unihub.data.repository

import com.example.unihub.data.api.RetrofitClient
import com.example.unihub.data.api.UniHubApi // Assume que esta é a sua interface de API
import com.example.unihub.data.api.model.LoginRequest
import com.example.unihub.data.api.model.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(
    private val api: UniHubApi = RetrofitClient.api // Assumindo uma classe para inicializar o Retrofit
) {
    suspend fun registerUser(
        name: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val request = RegisterRequest(name, email, password)
                val response = api.registerUser(request)

                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    onError(errorBody ?: "Erro desconhecido ao cadastrar usuário.")
                }
            } catch (e: HttpException) {
                // Erros de HTTP como 404, 401, etc.
                onError("Erro de servidor: ${e.code()}")
            } catch (e: IOException) {
                // Erros de conexão (sem internet, timeout, etc.)
                onError("Falha na conexão. Verifique sua rede.")
            } catch (e: Exception) {
                // Outros erros genéricos
                onError("Ocorreu um erro inesperado: ${e.message}")
            }
        }
    }

    suspend fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email, password)
                val response = api.loginUser(request)

                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    onError(errorBody ?: "Erro desconhecido ao fazer login.")
                }
            } catch (e: HttpException) {
                onError("Erro de servidor: ${e.code()}")
            } catch (e: IOException) {
                onError("Falha na conexão. Verifique sua rede.")
            } catch (e: Exception) {
                onError("Ocorreu um erro inesperado: ${e.message}")
            }
        }
    }
}