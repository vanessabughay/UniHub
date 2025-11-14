package com.example.unihub.data.apiBackend

import com.example.unihub.data.model.Instituicao
import com.example.unihub.data.api.InstituicaoApi
import com.example.unihub.data.config.RetrofitClient
import com.example.unihub.data.repository._instituicaobackend

class ApiInstituicaoBackend : _instituicaobackend {
    private val api: InstituicaoApi by lazy {
        RetrofitClient.create(InstituicaoApi::class.java)
    }

    override suspend fun buscarInstituicoesApi(query: String): List<Instituicao> =
        api.list(query)

    override suspend fun addInstituicaoApi(instituicao: Instituicao): Instituicao =
        api.add(instituicao)

    override suspend fun updateInstituicaoApi(id: Long, instituicao: Instituicao): Instituicao =
        api.update(id, instituicao)
}