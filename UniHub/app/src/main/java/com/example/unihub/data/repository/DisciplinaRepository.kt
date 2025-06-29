package com.example.unihub.data.repository // Exemplo de pacote

import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.unihub.data.model.Disciplina
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import retrofit2.HttpException

import com.example.unihub.data.model.HorarioAula

data class DisciplinaResumo(
    val disciplinaId: Long,
    val codigo: String,
    val nome: String,
    val aulas: List<HorarioAula>
)

//SUBTSTITUIR BACKEND
interface _disciplinabackend {
    suspend fun getDisciplinasResumoApi(): List<DisciplinaResumo>
    suspend fun getDisciplinaByIdApi(id: String): Disciplina?
    suspend fun addDisciplinaApi(disciplina: Disciplina)
    suspend fun updateDisciplinaApi(disciplina: Disciplina): Boolean
    suspend fun deleteDisciplinaApi(id: String): Boolean
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
    fun getDisciplinaById(id: String): Flow<Disciplina?> = flow {
        try {
            emit(backend.getDisciplinaByIdApi(id))
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
        return try {
            backend.updateDisciplinaApi(disciplina)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code ()}")
        }
    }

    //EXCLUSÃO DISCIPLINA
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun deleteDisciplina(id: String): Boolean {
        return try {
            backend.deleteDisciplinaApi(id)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }
}

