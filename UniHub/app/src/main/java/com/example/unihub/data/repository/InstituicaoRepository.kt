package com.example.unihub.data.repository

import android.os.Build
import android.content.Context
import androidx.annotation.RequiresExtension
import com.example.unihub.data.model.Instituicao
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
        val atualizada = try {
            backend.buscarInstituicoesApi("").firstOrNull()
        } catch (_: Exception) {
            null
        }

        if (atualizada != null) {
            instituicaoSelecionada = atualizada
            dataStore.edit { prefs ->
                prefs[INSTITUICAO_KEY] = gson.toJson(atualizada)
            }
        } else if (instituicaoSelecionada == null) {
            val prefs = dataStore.data.first()
            prefs[INSTITUICAO_KEY]?.let { json ->
                instituicaoSelecionada = gson.fromJson(json, Instituicao::class.java)
            }
        }
        return instituicaoSelecionada
    }

    suspend fun limparInstituicao() {
        dataStore.edit { prefs ->
            prefs.remove(INSTITUICAO_KEY)
        }
        instituicaoSelecionada = null
    }
}