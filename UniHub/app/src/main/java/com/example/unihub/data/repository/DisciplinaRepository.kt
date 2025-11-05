package com.example.unihub.data.repository

import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.unihub.data.model.Disciplina
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import retrofit2.HttpException

import com.example.unihub.data.model.HorarioAula

data class DisciplinaResumo( // conteudo resumo
    val id: Long,
    val codigo: String? = null,
    val nome: String,
    val aulas: List<HorarioAula> = emptyList(),
    val receberNotificacoes: Boolean = true,
    val totalAusencias: Int = 0,
    val ausenciasPermitidas: Int? = null,
    val isAtiva: Boolean
)

//SUBTSTITUIR BACKEND
interface _disciplinabackend {
    suspend fun getDisciplinasResumoApi(): List<DisciplinaResumo>
    suspend fun getDisciplinaByIdApi(id: String): Disciplina?
    suspend fun addDisciplinaApi(disciplina: Disciplina)
    suspend fun updateDisciplinaApi(id: Long, disciplina: Disciplina): Boolean
    suspend fun deleteDisciplinaApi(id: Long): Boolean
}

class DisciplinaRepository(private val backend: _disciplinabackend) {

    //LISTA RESUMO
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun getDisciplinasResumo(): Flow<List<DisciplinaResumo>> = flow {
        try {
            emit(backend.getDisciplinasResumoApi())
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}}")
        }
    }

    //BUSCA POR ID
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun getDisciplinaById(id: Long): Flow<Disciplina?> = flow {
        try {
            emit(backend.getDisciplinaByIdApi(id.toString()))
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    //ADD DISCIPLINA
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun addDisciplina(disciplina: Disciplina) {
        try {
            backend.addDisciplinaApi(disciplina)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    //PATCH DE ATUALIZAÇÃO DA DISCIPLINA
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun updateDisciplina(disciplina: Disciplina): Boolean {
        val id = disciplina.id ?: throw Exception("ID da disciplina não pode ser nulo para atualização.")
        return try {
            backend.updateDisciplinaApi(id, disciplina)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }



    //EXCLUSÃO DISCIPLINA
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun deleteDisciplina(id: String): Boolean {
        val longId = id.toLongOrNull() ?: throw Exception("ID inválido")
        return try {
            backend.deleteDisciplinaApi(longId)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }
}

