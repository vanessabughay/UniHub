package com.example.unihub.data.repository

import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.unihub.data.model.Instituicao
import retrofit2.HttpException
import java.io.IOException


interface _instituicaobackend {
    suspend fun buscarInstituicoesApi(query: String): List<Instituicao>
    suspend fun addInstituicaoApi(instituicao: Instituicao): Instituicao
    suspend fun updateInstituicaoApi(id: Int, instituicao: Instituicao): Instituicao
}

class InstituicaoRepository(private val backend: _instituicaobackend) {


    private var instituicaoSelecionada: Instituicao? = null

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun buscarInstituicoes(query: String): List<Instituicao> {
        return backend.buscarInstituicoesApi(query)


    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun getInstituicaoPorNome(nome: String): Result<Instituicao?> {
        return try {
            val instituicoesList = backend.buscarInstituicoesApi("")
            Result.success(instituicoesList.find { it.nome.equals(nome, ignoreCase = true) })
        } catch (e: IOException) {
            Result.failure(Exception("Erro de rede: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("Erro do servidor: ${e.code()}"))
        }
    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun salvarInstituicao(instituicao: Instituicao) {
        instituicaoSelecionada = if (instituicao.id != 0) {
            backend.updateInstituicaoApi(instituicao.id, instituicao)
        } else {
            backend.addInstituicaoApi(instituicao)
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun instituicaoUsuario(): Instituicao? = instituicaoSelecionada
}