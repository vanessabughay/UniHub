package com.example.unihub.data.repository

import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.unihub.data.model.Categoria
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

interface _categoriabackend {
    suspend fun listCategoriasApi(): List<Categoria>
    suspend fun addCategoriaApi(categoria: Categoria)
}

class CategoriaRepository(private val backend: _categoriabackend) {

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun listCategorias(): Flow<List<Categoria>> = flow {
        try {
            emit(backend.listCategoriasApi())
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun addCategoria(nomeDaCategoria: String) {
        val novaCategoria = Categoria(nome = nomeDaCategoria)
        try {
            backend.addCategoriaApi(novaCategoria)
        } catch (e: IOException) {
            throw Exception("Erro de rede: ${e.message}")
        } catch (e: HttpException) {
            throw Exception("Erro do servidor: ${e.code()}")
        }
    }
}