package com.example.unihub.data.repository

import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.unihub.data.model.Ausencia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

interface _ausenciabackend {
    suspend fun listAusenciasApi(): List<Ausencia>
    suspend fun getAusenciaByIdApi(id: Long): Ausencia?
    suspend fun addAusenciaApi(ausencia: Ausencia)
    suspend fun updateAusenciaApi(id: Long, ausencia: Ausencia): Boolean
    suspend fun deleteAusenciaApi(id: Long): Boolean
}

class AusenciaRepository(private val backend: _ausenciabackend) {

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun listAusencias(): Flow<List<Ausencia>> = flow {
        try {
            emit(backend.listAusenciasApi())
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun getAusenciaById(id: Long): Flow<Ausencia?> = flow {
        try {
            emit(backend.getAusenciaByIdApi(id))
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun addAusencia(ausencia: Ausencia) {
        try {
            backend.addAusenciaApi(ausencia)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun updateAusencia(ausencia: Ausencia): Boolean {
        val id = ausencia.id ?: throw Exception("ID da ausência não pode ser nulo para atualização.")
        return try {
            backend.updateAusenciaApi(id, ausencia)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun deleteAusencia(id: Long): Boolean = try {
        backend.deleteAusenciaApi(id)
    } catch (e: IOException) {
        throw Exception("Erro de rede: ${e.message}")
    } catch (e: HttpException) {
        throw Exception("Erro do servidor: ${e.code()}")
    }
}