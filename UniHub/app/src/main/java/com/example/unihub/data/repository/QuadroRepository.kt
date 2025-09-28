package com.example.unihub.data.repository

import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.unihub.data.model.Quadro
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import retrofit2.HttpException

data class QuadroResumo( // conteudo resumo
    val id: Long,
    val titulo: String? = null,
    val status: Estado,
    val data_criacao: Instant,
    val data_prazo: Instant,
    val disciplina_nome: String,
    val integrantes: String,
)

interface _quadrobackend {
    suspend fun getQuadrosResumoApi(): List<QuadroResumo>
    suspend fun getQuadroByIdApi(id: String): Quadro?
    suspend fun addQuadroApi(quadro: Quadro)
    suspend fun updateQuadroApi(id: Long, quadro: Quadro): Boolean
    suspend fun deleteQuadroApi(id: Long): Boolean
}

class QuadroRepository(private val backend: _quadrobackend) {

    //LISTA RESUMO
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun getQuadrosResumo(): Flow<List<QuadroResumo>> = flow {
        try {
            emit(backend.getQuadrosResumoApi())
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}}")
        }
    }

    //BUSCA POR ID
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun getQuadroById(id: Long): Flow<Quadro?> = flow {
        try {
            emit(backend.getQuadroByIdApi(id.toString()))
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    //ADD QUADRO
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun addQuadro(quadro: Quadro) {
        try {
            backend.addQuadroApi(quadro)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    //PATCH DE ATUALIZAÇÃO DA QUADRO
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun updateQuadro(quadro: Quadro): Boolean {
        val id = quadro.id ?: throw Exception("ID do quadro não pode ser nulo para atualização.")
        return try {
            backend.updateQuadroApi(id, quadro)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }



    //EXCLUSÃO QUADRO
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun deleteQuadro(id: String): Boolean {
        val longId = id.toLongOrNull() ?: throw Exception("ID inválido")
        return try {
            backend.deleteQuadroApi(longId)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }
}
