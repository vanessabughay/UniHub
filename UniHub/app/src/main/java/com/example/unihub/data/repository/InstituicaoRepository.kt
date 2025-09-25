package com.example.unihub.data.repository

import android.os.Build
import android.content.Context
import androidx.annotation.RequiresExtension
import com.example.unihub.data.model.Instituicao

interface _instituicaobackend {
    suspend fun buscarInstituicoesApi(query: String): List<Instituicao>
    suspend fun addInstituicaoApi(instituicao: Instituicao): Instituicao
    suspend fun updateInstituicaoApi(id: Long, instituicao: Instituicao): Instituicao
}

class InstituicaoRepository(
    private val backend: _instituicaobackend,
    context: Context
) {

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun buscarInstituicoes(query: String): List<Instituicao> {
        return backend.buscarInstituicoesApi(query)


    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun salvarInstituicao(instituicao: Instituicao) {
        if (instituicao.id != null) {
            backend.updateInstituicaoApi(instituicao.id!!, instituicao)
        } else {
            backend.addInstituicaoApi(instituicao)
        }

    }

    suspend fun instituicaoUsuario(): Instituicao? {
        return try {
            backend.buscarInstituicoesApi("").firstOrNull()
        } catch (_: Exception) {
            null
        }

    }
}