package com.example.unihub.data.apiBackend

import android.util.Log
import com.example.unihub.data.api.AvaliacaoApi
import com.example.unihub.data.config.RetrofitClient
import com.example.unihub.data.model.Avaliacao
import com.example.unihub.data.dto.AvaliacaoRequestDto
import com.example.unihub.data.dto.ContatoIdDto
import com.example.unihub.data.dto.DisciplinaIdDto
import com.example.unihub.data.model.Modalidade
import com.example.unihub.data.repository.Avaliacaobackend
import retrofit2.HttpException // Para tratar erros HTTP específicos do Retrofit
import java.io.IOException // Para exceções de I/O genéricas


// Não precisa de GsonBuilder aqui a menos que você tenha adaptadores de tipo específicos para Avaliacao
// import com.google.gson.GsonBuilder

class ApiAvaliacaoBackend : Avaliacaobackend { // Implementa sua interface Avaliacaobackend

    private val api: AvaliacaoApi by lazy {
        RetrofitClient.create(AvaliacaoApi::class.java)
    }

    private fun Avaliacao.toRequest(): AvaliacaoRequestDto {
        return AvaliacaoRequestDto(
            id = this.id,
            descricao = this.descricao,
            disciplina = this.disciplina?.id?.let { DisciplinaIdDto(it) },   // ← só id
            tipoAvaliacao = this.tipoAvaliacao,
            modalidade = this.modalidade ?: Modalidade.INDIVIDUAL,
            dataEntrega = this.dataEntrega,                                 // "yyyy-MM-dd"
            nota = this.nota,
            peso = this.peso,
            integrantes = (this.integrantes ?: emptyList())
                .mapNotNull { it.id }                                       // pega só ids
                .map { ContatoIdDto(it) },
            prioridade = this.prioridade,
            estado = this.estado,
            dificuldade = this.dificuldade,
            receberNotificacoes = this.receberNotificacoes
        )
    }

    override suspend fun getAvaliacaoApi(): List<Avaliacao> {
        try {
            val response = api.list() // Chama o método 'list' da AvaliacaoApi
            if (response.isSuccessful) {
                // Log opcional para verificar os dados brutos, se necessário
                response.body()?.forEachIndexed { index, avaliacao ->
                    Log.d("ApiAvaliacaoBackend", "Avaliacao da API ${index + 1}: ID=${avaliacao.id}, Nome='${avaliacao.descricao}', Membros=${avaliacao.integrantes?.size ?: "N/A"}")
                }
                return response.body() ?: emptyList() // Retorna a lista ou uma lista vazia se o corpo for nulo
            } else {
                // Lança uma exceção mais específica ou genérica baseada no erro
                Log.e("ApiAvaliacaoBackend", "Erro ao buscar avaliacaos: ${response.code()} - ${response.message()}")
                throw IOException("Erro na API ao buscar avaliacaos: ${response.code()} ${response.errorBody()?.string()}")
            }
        } catch (e: HttpException) {
            // Erro específico do Retrofit para respostas não bem-sucedidas (fora de 2xx)
            Log.e("ApiAvaliacaoBackend", "HttpException ao buscar avaliacaos: ${e.code()} - ${e.message()}", e)
            throw IOException("Erro de rede (HTTP ${e.code()}): ${e.message()}", e)
        } catch (e: IOException) {
            // Outros erros de I/O (ex: problema de conexão)
            Log.e("ApiAvaliacaoBackend", "IOException ao buscar avaliacaos", e)
            throw IOException("Erro de rede: ${e.message}", e)
        } catch (e: Exception) {
            // Captura genérica para outros erros inesperados
            Log.e("ApiAvaliacaoBackend", "Exceção inesperada ao buscar avaliacaos", e)
            throw IOException("Erro inesperado ao buscar avaliacaos: ${e.message}", e)
        }
    }

    override suspend fun getAvaliacaoByIdApi(id: String): Avaliacao? {
        try {
            val longId = id.toLongOrNull()
                ?: throw IllegalArgumentException("ID inválido fornecido: $id")
            val response = api.get(longId) // Chama o método 'get' da AvaliacaoApi
            if (response.isSuccessful) {
                return response.body()
            } else {
                Log.w("ApiAvaliacaoBackend", "Avaliacao não encontrado ou erro na API para ID $id: ${response.code()}")
                return null // Ou lançar uma exceção se um avaliacao não encontrado for um erro crítico aqui
            }
        } catch (e: NumberFormatException) {
            Log.e("ApiAvaliacaoBackend", "ID inválido para getAvaliacaoByIdApi: $id", e)
            throw IllegalArgumentException("Formato de ID inválido: $id", e)
        } catch (e: Exception) { // Captura mais genérica para erros de rede/API
            Log.e("ApiAvaliacaoBackend", "Exceção ao buscar avaliacao por ID $id", e)
            // Você pode querer relançar como IOException para ser tratado pelo Repository
            throw IOException("Erro ao buscar avaliacao por ID $id: ${e.message}", e)
        }
    }

    override suspend fun addAvaliacaoApi(request: AvaliacaoRequestDto) {
        val resp = api.add(request)
        if (!resp.isSuccessful) throw IOException("Erro: ${resp.code()} ${resp.errorBody()?.string()}")
    }

    override suspend fun updateAvaliacaoApi(id: Long, request: AvaliacaoRequestDto): Boolean {
        return api.update(id, request).isSuccessful
    }

    override suspend fun deleteAvaliacaoApi(id: Long): Boolean {
        try {
            val response = api.delete(id) // Chama o método 'delete' da AvaliacaoApi
            if (!response.isSuccessful) {
                Log.e("ApiAvaliacaoBackend", "Erro ao deletar avaliacao $id: ${response.code()} - ${response.message()}")
            }
            return response.isSuccessful
        } catch (e: Exception) {
            Log.e("ApiAvaliacaoBackend", "Exceção ao deletar avaliacao $id", e)
            // throw IOException("Erro ao deletar avaliacao $id: ${e.message}", e)
            return false
        }
    }

    override suspend fun getAvaliacaoPorDisciplinaApi(disciplinaId: Long): List<Avaliacao> {
        try {
            val resp = api.listPorDisciplina(disciplinaId)
            if (resp.isSuccessful) return resp.body() ?: emptyList()
            throw IOException("Erro listar por disciplina: ${resp.code()} ${resp.errorBody()?.string()}")
        } catch (e: Exception) {
            throw IOException("Falha rede/listar por disciplina: ${e.message}", e)
        }
    }
}

