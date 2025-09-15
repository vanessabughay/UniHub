package com.example.unihub.data.repository

import android.util.Log
import com.example.unihub.data.repository.ColunaApi
import com.example.unihub.data.model.Coluna

open class ColunaRepository(private val apiService: ColunaApi) {

    private val TAG = "ColunaRepository"

    suspend fun getColunas(): List<Coluna> {
        return try {
            apiService.getColunas()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar colunas: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getColunaById(colunaId: String): Coluna? {
        return try {
            apiService.getColunaById(colunaId)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar coluna por ID: ${e.message}", e)
            null
        }
    }

    suspend fun addColuna(coluna: Coluna): Coluna? {
        return try {
            apiService.addColuna(coluna)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao adicionar coluna: ${e.message}", e)
            null
        }
    }

    suspend fun updateColuna(coluna: Coluna): Coluna? {
        return try {
            apiService.updateColuna(coluna.id, coluna)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar coluna: ${e.message}", e)
            null
        }
    }

    suspend fun deleteColuna(colunaId: String) {
        try {
            apiService.deleteColuna(colunaId)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao excluir coluna: ${e.message}", e)
        }
    }
}