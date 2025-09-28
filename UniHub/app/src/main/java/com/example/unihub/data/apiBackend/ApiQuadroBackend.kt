package com.example.unihub.data.apiBackend

import com.example.unihub.data.api.QuadroApi
import com.example.unihub.data.config.RetrofitClient
import com.example.unihub.data.model.Quadro
import com.example.unihub.data.repository._quadrobackend
import java.io.IOException


class ApiQuadroBackend : _quadrobackend {
    private val api: QuadroApi by lazy {
        RetrofitClient.create(QuadroApi::class.java)
    }


    override suspend fun getQuadrosApi(): List<Quadro> {
        val response = api.getQuadros()
        if (!response.isSuccessful) {
            throw IOException("Erro ao listar quadros: ${response.code()} ${response.errorBody()?.string()}")
        }
        return response.body().orEmpty()
    }


    override suspend fun getQuadroByIdApi(id: String): Quadro? {
        val quadroId = id.toLongOrNull() ?: return null
        val response = api.getQuadroById(quadroId)
        if (response.isSuccessful) {
            return response.body()
        }
        if (response.code() == 404) {
            return null
        }
        throw IOException("Erro ao buscar quadro: ${response.code()} ${response.errorBody()?.string()}")
    }

    override suspend fun addQuadroApi(quadro: Quadro) {
        val response = api.addQuadro(quadro)
        if (!response.isSuccessful) {
            throw IOException("Erro ao adicionar quadro: ${response.code()} ${response.errorBody()?.string()}")
        }
    }

    override suspend fun updateQuadroApi(id: Long, quadro: Quadro): Boolean {
        val response = api.updateQuadro(id, quadro)
        if (!response.isSuccessful) {
            throw IOException("Erro ao atualizar quadro: ${response.code()} ${response.errorBody()?.string()}")
        }
        return true
    }

    override suspend fun deleteQuadroApi(id: Long): Boolean {
        val response = api.deleteQuadro(id)
        if (!response.isSuccessful) {
            throw IOException("Erro ao excluir quadro: ${response.code()} ${response.errorBody()?.string()}")
        }
        return true
    }


}