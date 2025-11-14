package com.example.unihub.data.apiBackend

import com.example.unihub.data.api.AnotacoesApi
import com.example.unihub.data.api.AusenciaApi
import com.example.unihub.data.config.RetrofitClient
import com.example.unihub.data.dto.AnotacoesRequest
import com.example.unihub.data.model.Anotacao
import com.example.unihub.data.model.Ausencia
import com.example.unihub.data.repository._ausenciabackend

class ApiAusenciaBackend : _ausenciabackend {
    private val api: AusenciaApi by lazy {
        RetrofitClient.create(AusenciaApi::class.java)
    }

    override suspend fun listAusenciasApi(): List<Ausencia> = api.list()

    override suspend fun getAusenciaByIdApi(id: Long): Ausencia? = api.get(id)

    override suspend fun addAusenciaApi(ausencia: Ausencia) {
        api.add(ausencia)
    }

    override suspend fun updateAusenciaApi(id: Long, ausencia: Ausencia): Boolean {
        api.update(id, ausencia)
        return true
    }

    override suspend fun deleteAusenciaApi(id: Long): Boolean {
        api.delete(id)
        return true
    }
}

class ApiAnotacoesBackend {

    private val api: AnotacoesApi by lazy {
        RetrofitClient.create(AnotacoesApi::class.java)
    }


    suspend fun listAnotacoesApi(disciplinaId: Long): List<Anotacao> {
        val response = api.listar(disciplinaId)
        return response.content.map { dto ->
            Anotacao(
                id = dto.id,
                titulo = dto.titulo,
                conteudo = dto.conteudo
            )
        }
    }

    suspend fun getAnotacaoByIdApi(disciplinaId: Long, anotacaoId: Long): Anotacao? {
        return api.obter(disciplinaId, anotacaoId)?.let { dto ->
            Anotacao(
                id = dto.id,
                titulo = dto.titulo,
                conteudo = dto.conteudo
            )
        }
    }

    suspend fun addAnotacaoApi(disciplinaId: Long, request: AnotacoesRequest): Anotacao {
        val dto = api.criar(disciplinaId, request)
        return Anotacao(
            id = dto.id,
            titulo = dto.titulo,
            conteudo = dto.conteudo
        )
    }

    suspend fun updateAnotacaoApi(disciplinaId: Long, anotacaoId: Long, request: AnotacoesRequest): Anotacao {
        val dto = api.atualizar(disciplinaId, anotacaoId, request)
        return Anotacao(
            id = dto.id,
            titulo = dto.titulo,
            conteudo = dto.conteudo
        )
    }

    suspend fun deleteAnotacaoApi(disciplinaId: Long, anotacaoId: Long) {
        api.excluir(disciplinaId, anotacaoId)
    }
}