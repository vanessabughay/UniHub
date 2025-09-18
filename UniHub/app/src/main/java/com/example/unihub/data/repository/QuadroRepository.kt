package com.example.unihub.data.repository

import android.util.Log
import com.example.unihub.data.repository.QuadroApi
import com.example.unihub.data.model.QuadroDePlanejamento

open class QuadroRepository(private val apiService: QuadroApi) {

    private val TAG = "QuadroRepository"

    suspend fun getQuadros(): List<QuadroDePlanejamento> {
        return try {
            apiService.getQuadros()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar quadros: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getQuadroById(quadroId: String): QuadroDePlanejamento? {
        return try {
            apiService.getQuadroById(quadroId)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar quadro por ID: ${e.message}", e)
            null
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

    suspend fun updateQuadro(quadro: QuadroDePlanejamento): QuadroDePlanejamento? {
        return try {
            apiService.updateQuadro(quadro.id, quadro)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar quadro: ${e.message}", e)
            null
        }
    }

    suspend fun deleteQuadro(quadroId: String) {
        try {
            apiService.deleteQuadro(quadroId)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao excluir quadro: ${e.message}", e)
        }
    }
}