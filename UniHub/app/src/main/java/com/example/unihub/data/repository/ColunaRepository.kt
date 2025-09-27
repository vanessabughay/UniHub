package com.example.unihub.data.repository

import android.util.Log
import com.example.unihub.data.api.ColunaApi
import com.example.unihub.data.model.Coluna

open class ColunaRepository(private val apiService: ColunaApi) {

    private val TAG = "ColunaRepository"

    //  Agora recebe o quadroId
    suspend fun getColunas(quadroId: String): List<Coluna> {
        return try {
            apiService.getColunas(quadroId)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar colunas do quadro $quadroId: ${e.message}", e)
            emptyList()
        }
    }

    // Agora recebe o quadroId
    suspend fun getColunaById(quadroId: String, colunaId: String): Coluna? {
        return try {
            apiService.getColunaById(quadroId, colunaId)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar coluna $colunaId do quadro $quadroId: ${e.message}", e)
            null
        }
    }

    //  Agora recebe o quadroId
    suspend fun addColuna(quadroId: String, coluna: Coluna): Coluna? {
        return try {
            apiService.addColuna(quadroId, coluna)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao adicionar coluna no quadro $quadroId: ${e.message}", e)
            null
        }
    }

    //  Agora recebe o quadroId
    suspend fun updateColuna(quadroId: String, coluna: Coluna): Coluna? {
        return try {
            apiService.updateColuna(quadroId, coluna.id, coluna)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar coluna ${coluna.id} no quadro $quadroId: ${e.message}", e)
            null
        }
    }

    // Agora recebe o quadroId
    suspend fun deleteColuna(quadroId: String, colunaId: String) {
        try {
            apiService.deleteColuna(quadroId, colunaId)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao excluir coluna $colunaId do quadro $quadroId: ${e.message}", e)
        }
    }
}