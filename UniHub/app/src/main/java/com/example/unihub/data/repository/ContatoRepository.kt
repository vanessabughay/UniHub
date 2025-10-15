package com.example.unihub.data.repository

import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.unihub.data.model.Contato
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException


data class ContatoResumo(
    val id: Long,
    val nome: String,
    val email: String,
    val pendente: Boolean
)


interface Contatobackend { // Removi o "_" inicial, é uma convenção melhor
    suspend fun getContatoResumoApi(): List<ContatoResumo>
    suspend fun getContatoByIdApi(id: String): Contato?
    suspend fun addContatoApi(contato: Contato)
    suspend fun updateContatoApi(id: Long, contato: Contato): Boolean
    suspend fun deleteContatoApi(id: Long): Boolean
}

// Esta é agora a única classe ContatoRepository
class ContatoRepository(private val backend: Contatobackend) {

    //LISTA RESUMO
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun getContatoResumo(): Flow<List<ContatoResumo>> = flow {
        try {
            emit(backend.getContatoResumoApi())
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    //BUSCA POR ID
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun getContatoById(id: Long): Flow<Contato?> = flow {
        try {
            emit(backend.getContatoByIdApi(id.toString()))
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    suspend fun fetchContatoById(id: Long): Contato? {
        return try {
            backend.getContatoByIdApi(id.toString())
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    //ADD CONTATO
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun addContato(contato: Contato) {
        try {
            backend.addContatoApi(contato)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    //PATCH DE ATUALIZAÇÃO DO CONTATO
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun updateContato(contato: Contato): Boolean {
        val id = contato.id ?: throw Exception("ID do contato não pode ser nulo para atualização.")
        return try {
            backend.updateContatoApi(id, contato)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    //EXCLUSÃO CONTATO
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun deleteContato(id: String): Boolean {
        val longId = id.toLongOrNull() ?: throw Exception("ID inválido")
        return try {
            backend.deleteContatoApi(longId)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }
}
