package com.example.unihub.data.repository

import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.unihub.data.model.Grupo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException



interface Grupobackend { // Removi o "_" inicial, é uma convenção melhor
    suspend fun getGrupoApi(): List<Grupo>
    suspend fun getGrupoByIdApi(id: String): Grupo?
    suspend fun addGrupoApi(grupo: Grupo)
    suspend fun updateGrupoApi(id: Long, grupo: Grupo): Boolean
    suspend fun deleteGrupoApi(id: Long): Boolean
    suspend fun leaveGrupoApi(id: Long): Boolean
}

// Esta é agora a única classe GrupoRepository
class GrupoRepository(private val backend: Grupobackend) {

    //LISTA
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun getGrupo(): Flow<List<Grupo>> = flow {
        try {
            emit(backend.getGrupoApi())
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    //BUSCA POR ID
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun getGrupoById(id: Long): Flow<Grupo?> = flow {
        try {
            emit(backend.getGrupoByIdApi(id.toString()))
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    suspend fun fetchGrupoById(id: Long): Grupo? {
        return try {
            backend.getGrupoByIdApi(id.toString())
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    //ADD Grupo
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun addGrupo(grupo: Grupo) {
        try {
            backend.addGrupoApi(grupo)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    //PATCH DE ATUALIZAÇÃO DO Grupo
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun updateGrupo(grupo: Grupo): Boolean {
        val id = grupo.id ?: throw Exception("ID do Grupo não pode ser nulo para atualização.")
        return try {
            backend.updateGrupoApi(id, grupo)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    //EXCLUSÃO Grupo
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun deleteGrupo(id: String): Boolean {
        val longId = id.toLongOrNull() ?: throw IllegalArgumentException("ID de Grupo inválido.")
        return backend.deleteGrupoApi(longId)
    }
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun leaveGrupo(id: String): Boolean {
        val longId = id.toLongOrNull() ?: throw IllegalArgumentException("ID de Grupo inválido.")
        return backend.leaveGrupoApi(longId)
    }
}