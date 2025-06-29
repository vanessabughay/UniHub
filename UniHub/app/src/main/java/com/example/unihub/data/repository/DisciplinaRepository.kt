package com.example.unihub.data.repository

import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.model.HorarioAula // Certifique-se de que HorarioAula está disponível
import com.example.unihub.data.remote.DisciplinaApiService // Importe a interface do Retrofit
import com.example.unihub.data.remote.DisciplinaResumo // Importe o DisciplinaResumo do pacote remote se definido lá

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import retrofit2.HttpException
import java.lang.IllegalArgumentException // Importar explicitamente


// O repositório agora recebe a implementação da API (gerada pelo Retrofit)
class DisciplinaRepository(private val apiService: DisciplinaApiService) {

    // LISTA RESUMO
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun getDisciplinasResumo(): Flow<List<DisciplinaResumo>> = flow {
        try {
            val response = apiService.getDisciplinasResumoApi()
            if (response.isSuccessful) {
                emit(response.body() ?: emptyList())
            } else {
                val errorMessage = "Erro ao buscar resumo de disciplinas: ${response.code()} - ${response.errorBody()?.string()}"
                throw HttpException(response)
            }
        } catch (e: IOException) {
            throw Exception("Erro de rede ao buscar resumo de disciplinas: ${e.message}", e)
        } catch (e: HttpException) {
            throw Exception("Erro do servidor ao buscar resumo de disciplinas: ${e.code()}", e)
        } catch (e: Exception) {
            throw Exception("Erro inesperado ao buscar resumo de disciplinas: ${e.message}", e)
        }
    }

    // BUSCA POR ID
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun getDisciplinaById(id: String): Flow<Disciplina?> = flow {
        try {
            val response = apiService.getDisciplinaByIdApi(id)
            if (response.isSuccessful) {
                emit(response.body())
            } else {
                val errorMessage = "Erro ao buscar disciplina por ID: ${response.code()} - ${response.errorBody()?.string()}"
                throw HttpException(response)
            }
        } catch (e: IOException) {
            throw Exception("Erro de rede ao buscar disciplina por ID: ${e.message}", e)
        } catch (e: HttpException) {
            throw Exception("Erro do servidor ao buscar disciplina por ID: ${e.code()}", e)
        } catch (e: Exception) {
            throw Exception("Erro inesperado ao buscar disciplina por ID: ${e.message}", e)
        }
    }

    // ADD DISCIPLINA
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun addDisciplina(disciplina: Disciplina) {
        try {
            val response = apiService.addDisciplinaApi(disciplina)
            if (!response.isSuccessful) {
                val errorMessage = "Erro ao adicionar disciplina: ${response.code()} - ${response.errorBody()?.string()}"
                throw Exception(errorMessage)
            }
        } catch (e: IOException) {
            throw Exception("Erro de rede ao adicionar disciplina: ${e.message}", e)
        } catch (e: HttpException) {
            throw Exception("Erro do servidor ao adicionar disciplina: ${e.code()}", e)
        } catch (e: Exception) {
            throw Exception("Erro inesperado ao adicionar disciplina: ${e.message}", e)
        }
    }

    // PATCH DE ATUALIZAÇÃO DA DISCIPLINA
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun updateDisciplina(disciplina: Disciplina): Boolean {
        return try {
            val id = disciplina.id
            if (id == null || id.isEmpty()) {
                throw IllegalArgumentException("ID da disciplina não pode ser nulo ou vazio para atualização.")
            }
            val response = apiService.updateDisciplinaApi(id, disciplina)
            if (!response.isSuccessful) {
                val errorMessage = "Erro ao atualizar disciplina: ${response.code()} - ${response.errorBody()?.string()}"
                throw Exception(errorMessage)
            }
            true
        } catch (e: IOException) {
            throw Exception("Erro de rede ao atualizar disciplina: ${e.message}", e)
        } catch (e: HttpException) {
            throw Exception("Erro do servidor ao atualizar disciplina: ${e.code()}", e)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Erro inesperado ao atualizar disciplina: ${e.message}", e)
        }
    }

    // EXCLUSÃO DISCIPLINA
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun deleteDisciplina(id: String): Boolean {
        return try {
            val response = apiService.deleteDisciplinaApi(id)
            if (!response.isSuccessful) {
                val errorMessage = "Erro ao deletar disciplina: ${response.code()} - ${response.errorBody()?.string()}"
                throw Exception(errorMessage)
            }
            true
        } catch (e: IOException) {
            throw Exception("Erro de rede ao deletar disciplina: ${e.message}", e)
        } catch (e: HttpException) {
            throw Exception("Erro do servidor ao deletar disciplina: ${e.code()}", e)
        } catch (e: Exception) {
            throw Exception("Erro inesperado ao deletar disciplina: ${e.message}", e)
        }
    }
}