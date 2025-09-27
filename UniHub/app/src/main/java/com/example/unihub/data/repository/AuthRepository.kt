package com.example.unihub.data.repository

import com.example.unihub.data.config.RetrofitClient
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.api.UniHubApi // Assume que esta é a sua interface de API
import com.example.unihub.data.api.model.LoginRequest
import com.example.unihub.data.api.model.RegisterRequest
import com.example.unihub.data.api.model.UpdateUsuarioRequest
import com.example.unihub.data.api.model.SolicitarRedefinicaoSenhaRequest
import com.example.unihub.data.api.model.RedefinirSenhaRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import android.content.Context

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
                    withContext(Dispatchers.Main) { onSuccess() }
                } else {
                    val errorBody = response.errorBody()?.string()
                    withContext(Dispatchers.Main) {
                        onError(errorBody ?: "Erro desconhecido ao cadastrar usuário.")
                    }                }
            } catch (e: HttpException) {
                // Erros de HTTP como 404, 401, etc.
                withContext(Dispatchers.Main) { onError("Erro de servidor: ${e.code()}") }
            } catch (e: IOException) {
                // Erros de conexão (sem internet, timeout, etc.)
                withContext(Dispatchers.Main) { onError("Falha na conexão. Verifique sua rede.") }
            } catch (e: Exception) {
                // Outros erros genéricos
                withContext(Dispatchers.Main) { onError("Ocorreu um erro inesperado: ${e.message}") }
            }
        }
    }

    suspend fun loginUser(
        context: Context,
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
                    val authResponse = response.body()
                    if (authResponse != null) {
                        TokenManager.saveToken(
                            context,
                            authResponse.token,
                            authResponse.nomeUsuario,
                            email
                        )
                        withContext(Dispatchers.Main) { onSuccess() }
                    } else {
                        withContext(Dispatchers.Main) {
                            onError("Token não encontrado na resposta.")
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    withContext(Dispatchers.Main) {
                        onError(errorBody ?: "Erro desconhecido ao fazer login.")
                    }
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) { onError("Erro de servidor: ${e.code()}") }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) { onError("Falha na conexão. Verifique sua rede.") }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError("Ocorreu um erro inesperado: ${e.message}") }
            }
        }
    }

    suspend fun updateUser(
        context: Context,
        name: String,
        email: String,
        password: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val request = UpdateUsuarioRequest(name, email, password)
                val token = TokenManager.token

                if (token.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        onError("Token inválido. Faça login novamente.")
                    }
                    return@withContext
                }
                val response = api.updateUser("Bearer $token", request)
                if (response.isSuccessful) {
                    TokenManager.saveToken(context, token, name, email)
                    withContext(Dispatchers.Main) { onSuccess() }
                } else {
                    val errorBody = response.errorBody()?.string()
                    withContext(Dispatchers.Main) {
                        onError(errorBody ?: "Erro desconhecido ao atualizar usuário.")
                    }
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) { onError("Erro de servidor: ${e.code()}") }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) { onError("Falha na conexão. Verifique sua rede.") }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError("Ocorreu um erro inesperado: ${e.message}") }
            }
        }
    }

    suspend fun solicitarRedefinicaoSenha(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val resp = api.solicitarRedefinicaoSenha(
                    SolicitarRedefinicaoSenhaRequest(email = email.trim())
                )
                if (resp.isSuccessful) {
                    withContext(Dispatchers.Main) { onSuccess() }
                } else {
                    val msg = resp.errorBody()?.string() ?: "Falha ao solicitar redefinição."
                    withContext(Dispatchers.Main) { onError(msg) }
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) { onError("Erro de servidor: ${e.code()}") }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) { onError("Falha na conexão. Verifique sua rede.") }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError("Erro inesperado: ${e.message}") }
            }
        }
    }

    suspend fun redefinirSenha(
        token: String,
        novaSenha: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val resp = api.redefinirSenha(
                    RedefinirSenhaRequest(token = token, novaSenha = novaSenha)
                )
                if (resp.isSuccessful) {
                    withContext(Dispatchers.Main) { onSuccess() }
                } else {
                    val msg = resp.errorBody()?.string() ?: "Falha ao redefinir senha."
                    withContext(Dispatchers.Main) { onError(msg) }
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) { onError("Erro de servidor: ${e.code()}") }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) { onError("Falha na conexão. Verifique sua rede.") }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError("Erro inesperado: ${e.message}") }
            }
        }
    }

}