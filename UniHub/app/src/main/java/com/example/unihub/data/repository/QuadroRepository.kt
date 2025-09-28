package com.example.unihub.data.repository

import android.util.Log
import com.example.unihub.data.api.QuadroApi
import com.example.unihub.data.model.QuadroDePlanejamento
import retrofit2.HttpException

open class QuadroRepository(private val apiService: QuadroApi) {

    private val TAG = "QuadroRepository"

    suspend fun getQuadros(): List<QuadroDePlanejamento> {
        return try {
            apiService.getQuadros()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar quadros", e)
            throw e
        }
    }

    suspend fun getQuadroById(quadroId: String): QuadroDePlanejamento? {
        return try {
            apiService.getQuadroById(quadroId)
        } catch (e: HttpException) {
            if (e.code() == 404) {
                null
            } else {
                Log.e(TAG, "Erro ao buscar quadro por ID", e)
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar quadro por ID", e)
            throw e
        }
    }

    suspend fun addQuadro(quadro: QuadroDePlanejamento): QuadroDePlanejamento? {
        return try {
            apiService.addQuadro(quadro)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao adicionar quadro: ${e.message}", e)
            null
        }
    }

    suspend fun updateQuadro(quadro: QuadroDePlanejamento): QuadroDePlanejamento {
        val quadroId = quadro.id ?: throw IllegalArgumentException("ID do quadro é obrigatório para atualização.")
        return try {
            apiService.updateQuadro(quadroId, quadro)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar quadro", e)
            throw e
        }
    }

    suspend fun deleteQuadro(quadroId: String) {
        try {
            apiService.deleteQuadro(quadroId)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao excluir quadro", e)
            throw e
        }
    }
}