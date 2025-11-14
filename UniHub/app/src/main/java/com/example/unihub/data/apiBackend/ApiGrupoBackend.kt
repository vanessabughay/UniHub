package com.example.unihub.data.apiBackend

import android.util.Log
import com.example.unihub.data.api.GrupoApi
import com.example.unihub.data.config.RetrofitClient
import com.example.unihub.data.model.Grupo
import com.example.unihub.data.repository.Grupobackend
import retrofit2.HttpException // Para tratar erros HTTP específicos do Retrofit
import java.io.IOException // Para exceções de I/O genéricas


// Não precisa de GsonBuilder aqui a menos que você tenha adaptadores de tipo específicos para Grupo
// import com.google.gson.GsonBuilder

class ApiGrupoBackend : Grupobackend { // Implementa sua interface Grupobackend

    private val api: GrupoApi by lazy {
        RetrofitClient.create(GrupoApi::class.java)
    }

    override suspend fun getGrupoApi(): List<Grupo> {
        try {
            val response = api.list() // Chama o método 'list' da GrupoApi
            if (response.isSuccessful) {
                // Log opcional para verificar os dados brutos, se necessário
                response.body()?.forEachIndexed { index, grupo ->
                    Log.d("ApiGrupoBackend", "Grupo da API ${index + 1}: ID=${grupo.id}, Nome='${grupo.nome}', Membros=${grupo.membros?.size ?: "N/A"}")
                }
                return response.body() ?: emptyList() // Retorna a lista ou uma lista vazia se o corpo for nulo
            } else {
                // Lança uma exceção mais específica ou genérica baseada no erro
                Log.e("ApiGrupoBackend", "Erro ao buscar grupos: ${response.code()} - ${response.message()}")
                throw IOException("Erro na API ao buscar grupos: ${response.code()} ${response.errorBody()?.string()}")
            }
        } catch (e: HttpException) {
            // Erro específico do Retrofit para respostas não bem-sucedidas (fora de 2xx)
            Log.e("ApiGrupoBackend", "HttpException ao buscar grupos: ${e.code()} - ${e.message()}", e)
            throw IOException("Erro de rede (HTTP ${e.code()}): ${e.message()}", e)
        } catch (e: IOException) {
            // Outros erros de I/O (ex: problema de conexão)
            Log.e("ApiGrupoBackend", "IOException ao buscar grupos", e)
            throw IOException("Erro de rede: ${e.message}", e)
        } catch (e: Exception) {
            // Captura genérica para outros erros inesperados
            Log.e("ApiGrupoBackend", "Exceção inesperada ao buscar grupos", e)
            throw IOException("Erro inesperado ao buscar grupos: ${e.message}", e)
        }
    }

    override suspend fun getGrupoByIdApi(id: String): Grupo? {
        try {
            val longId = id.toLongOrNull()
                ?: throw IllegalArgumentException("ID inválido fornecido: $id")
            val response = api.get(longId) // Chama o método 'get' da GrupoApi
            if (response.isSuccessful) {
                return response.body()
            } else {
                Log.w("ApiGrupoBackend", "Grupo não encontrado ou erro na API para ID $id: ${response.code()}")
                return null // Ou lançar uma exceção se um grupo não encontrado for um erro crítico aqui
            }
        } catch (e: NumberFormatException) {
            Log.e("ApiGrupoBackend", "ID inválido para getGrupoByIdApi: $id", e)
            throw IllegalArgumentException("Formato de ID inválido: $id", e)
        } catch (e: Exception) { // Captura mais genérica para erros de rede/API
            Log.e("ApiGrupoBackend", "Exceção ao buscar grupo por ID $id", e)
            // Você pode querer relançar como IOException para ser tratado pelo Repository
            throw IOException("Erro ao buscar grupo por ID $id: ${e.message}", e)
        }
    }

    override suspend fun addGrupoApi(grupo: Grupo) { // Retorno Unit (void) como na interface
        try {
            val response = api.add(grupo) // Chama o método 'add' da GrupoApi
            if (!response.isSuccessful) {
                Log.e("ApiGrupoBackend", "Erro ao adicionar grupo: ${response.code()} - ${response.message()}")
                throw IOException("Erro na API ao adicionar grupo: ${response.code()} ${response.errorBody()?.string()}")
            }
            // Opcional: Logar o grupo retornado pela API (pode ter ID gerado, etc.)
            Log.d("ApiGrupoBackend", "Grupo adicionado com sucesso: ${response.body()}")
        } catch (e: Exception) {
            Log.e("ApiGrupoBackend", "Exceção ao adicionar grupo", e)
            throw IOException("Erro ao adicionar grupo: ${e.message}", e)
        }
    }

    override suspend fun updateGrupoApi(id: Long, grupo: Grupo): Boolean {
        try {
            val response = api.update(id, grupo) // Chama o método 'update' da GrupoApi
            if (!response.isSuccessful) {
                Log.e("ApiGrupoBackend", "Erro ao atualizar grupo $id: ${response.code()} - ${response.message()}")
            }
            return response.isSuccessful
        } catch (e: Exception) {
            Log.e("ApiGrupoBackend", "Exceção ao atualizar grupo $id", e)
            // Você pode querer relançar como IOException para ser tratado pelo Repository
            // throw IOException("Erro ao atualizar grupo $id: ${e.message}", e)
            return false // Retorna false em caso de exceção
        }
    }

    override suspend fun deleteGrupoApi(id: Long): Boolean {
        try {
            val response = api.delete(id)
            if (response.isSuccessful) {
                return true
            }
            val errorMessage = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
            if (response.code() == 409) {
                val mensagem = errorMessage ?: "Não foi possível transferir a propriedade do grupo para outro membro."
                Log.w("ApiGrupoBackend", "Falha ao remover grupo $id: $mensagem")
                throw IllegalStateException(mensagem)
            }

            Log.e(
                "ApiGrupoBackend",
                "Erro ao deletar grupo $id: ${response.code()} - ${response.message()}${errorMessage?.let { ": $it" } ?: ""}"
            )
            throw HttpException(response)
        } catch (e: IllegalStateException) {
            throw e
        } catch (e: HttpException) {
            Log.e("ApiGrupoBackend", "HttpException ao deletar grupo $id", e)
            throw e
        } catch (e: IOException) {
            Log.e("ApiGrupoBackend", "IOException ao deletar grupo $id", e)
            throw e
        } catch (e: Exception) {
            Log.e("ApiGrupoBackend", "Exceção inesperada ao deletar grupo $id", e)
            throw IOException("Erro ao deletar grupo $id: ${e.message}", e)

        }
    }
    override suspend fun leaveGrupoApi(id: Long): Boolean {
        try {
            val response = api.leave(id)
            if (response.isSuccessful) {
                return true
            }
            val errorMessage = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
            Log.w(
                "ApiGrupoBackend",
                "Falha ao sair do grupo $id: ${response.code()} - ${response.message()}${errorMessage?.let { ": $it" } ?: ""}"
            )
            throw HttpException(response)
        } catch (e: HttpException) {
            throw e
        } catch (e: IOException) {
            Log.e("ApiGrupoBackend", "IOException ao sair do grupo $id", e)
            throw e
        } catch (e: Exception) {
            Log.e("ApiGrupoBackend", "Exceção inesperada ao sair do grupo $id", e)
            throw IOException("Erro ao sair do grupo $id: ${e.message}", e)
        }
    }
}

