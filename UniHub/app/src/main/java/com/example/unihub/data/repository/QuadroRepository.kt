package com.example.unihub.data.repository

import com.example.unihub.data.model.Quadro

import java.io.IOException
import retrofit2.HttpException

interface _quadrobackend {
    suspend fun getQuadrosApi(): List<Quadro>
    suspend fun getQuadroByIdApi(id: String): Quadro?
    suspend fun addQuadroApi(quadro: Quadro)
    suspend fun updateQuadroApi(id: Long, quadro: Quadro): Boolean
    suspend fun deleteQuadroApi(id: Long): Boolean
}

class QuadroRepository(private val backend: _quadrobackend) {

    suspend fun getQuadros(): List<Quadro> {
        return try {
            backend.getQuadrosApi()
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    suspend fun getQuadroById(id: String): Quadro? {
        return try {
            backend.getQuadroByIdApi(id)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    suspend fun addQuadro(quadro: Quadro) {
        try {
            backend.addQuadroApi(quadro)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }


    suspend fun updateQuadro(quadro: Quadro): Boolean {
        val id = quadro.id?.toLongOrNull() ?: throw Exception("ID do quadro não pode ser nulo para atualização.")
        return try {
            backend.updateQuadroApi(id, quadro)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }




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
