package com.example.unihub.data.repository

import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.unihub.data.dto.AvaliacaoRequest
import com.example.unihub.data.model.Avaliacao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException




interface Avaliacaobackend { // Removi o "_" inicial, é uma convenção melhor
    suspend fun getAvaliacaoApi(): List<Avaliacao>
    suspend fun getAvaliacaoByIdApi(id: String): Avaliacao?
    suspend fun addAvaliacaoApi(request: AvaliacaoRequest)
    suspend fun updateAvaliacaoApi(id: Long, request: AvaliacaoRequest): Boolean
    suspend fun deleteAvaliacaoApi(id: Long): Boolean

}

// Esta é agora a única classe AvaliacaoRepository
class AvaliacaoRepository(private val backend: Avaliacaobackend) {

    //LISTA
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun getAvaliacao(): Flow<List<Avaliacao>> = flow {
        try {
            emit(backend.getAvaliacaoApi())
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    //BUSCA POR ID
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun getAvaliacaoById(id: Long): Flow<Avaliacao?> = flow {
        try {
            emit(backend.getAvaliacaoByIdApi(id.toString()))
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    //ADD Avaliacao
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun addAvaliacao(request: AvaliacaoRequest) {
        backend.addAvaliacaoApi(request)
    }

    //PATCH DE ATUALIZAÇÃO DO Avaliacao
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun updateAvaliacao(id: Long, request: AvaliacaoRequest): Boolean {
        return backend.updateAvaliacaoApi(id, request)
    }

    //EXCLUSÃO Avaliacao
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun deleteAvaliacao(id: String): Boolean {
        val longId = id.toLongOrNull() ?: throw Exception("ID inválido")
        return try {
            backend.deleteAvaliacaoApi(longId)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }
}