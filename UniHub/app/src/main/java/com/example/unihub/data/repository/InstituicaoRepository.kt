package com.example.unihub.data.repository

import android.os.Build
import android.content.Context
import androidx.annotation.RequiresExtension
import com.example.unihub.data.model.Instituicao
import retrofit2.HttpException
import java.io.IOException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first


interface _instituicaobackend {
    suspend fun buscarInstituicoesApi(query: String): List<Instituicao>
    suspend fun addInstituicaoApi(instituicao: Instituicao): Instituicao
    suspend fun updateInstituicaoApi(id: Long, instituicao: Instituicao): Instituicao
}

private val Context.instituicaoDataStore: DataStore<Preferences> by preferencesDataStore(name = "instituicao_prefs")

class InstituicaoRepository(
    private val backend: _instituicaobackend,
    private val context: Context
) {

    private val dataStore = context.instituicaoDataStore
    private val gson = Gson()
    private val INSTITUICAO_KEY = stringPreferencesKey("instituicao")
    private var instituicaoSelecionada: Instituicao? = null

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun buscarInstituicoes(query: String): List<Instituicao> {
        return backend.buscarInstituicoesApi(query)


    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun getInstituicaoPorNome(nome: String): Result<Instituicao?> {
        return try {
            val instituicoesList = backend.buscarInstituicoesApi(nome)
            Result.success(instituicoesList.firstOrNull { it.nome.equals(nome, ignoreCase = true) })
        } catch (e: IOException) {
            Result.failure(Exception("Erro de rede: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("Erro do servidor: ${e.code()}"))
        }
    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun salvarInstituicao(instituicao: Instituicao) {
        instituicaoSelecionada = if (instituicao.id != null) {
            backend.updateInstituicaoApi(instituicao.id!!, instituicao)
        } else {
            backend.addInstituicaoApi(instituicao)
        }
        dataStore.edit { prefs ->
            prefs[INSTITUICAO_KEY] = gson.toJson(instituicaoSelecionada)
        }
    }

    suspend fun instituicaoUsuario(): Instituicao? {
        if (instituicaoSelecionada == null) {
            val prefs = dataStore.data.first()
            prefs[INSTITUICAO_KEY]?.let { json ->
                instituicaoSelecionada = gson.fromJson(json, Instituicao::class.java)
            }
        }
        return instituicaoSelecionada
    }
}