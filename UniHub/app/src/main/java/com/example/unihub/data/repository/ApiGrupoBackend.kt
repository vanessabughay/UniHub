package com.example.unihub.data.repository

import android.util.Log // Certifique-se de que esta importação está presente
import com.example.unihub.data.model.Grupo
import com.example.unihub.data.util.LocalDateAdapter
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate

class ApiGrupoBackend : Grupobackend {

    private val api: GrupoApi by lazy {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .create()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") // Verifique esta URL!
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(GrupoApi::class.java)
    }

    override suspend fun getGrupoApi(): List<Grupo> {
        Log.d("ApiGrupoBackend", "Iniciando getGrupoApi...")
        try {
            val gruposDaApi = api.list() // api.list() deve ser uma suspend function
            Log.d("ApiGrupoBackend", "Chamada api.list() concluída.")

            if (gruposDaApi == null) {
                Log.w("ApiGrupoBackend", "API retornou uma lista nula (gruposDaApi é null). Retornando lista vazia.")
                return emptyList()
            }

            Log.d("ApiGrupoBackend", "Número de grupos recebidos da API: ${gruposDaApi.size}")
            if (gruposDaApi.isEmpty()) {
                Log.d("ApiGrupoBackend", "API retornou uma lista vazia. Retornando lista vazia.")
                return emptyList()
            }

            // Logar cada DTO recebido antes da transformação
            gruposDaApi.forEachIndexed { index, grupoDto ->
                Log.d("ApiGrupoBackend", "DTO $index: id=${grupoDto.id}, nome='${grupoDto.nome}', membros=${grupoDto.membros?.size ?: "null"}")
            }

            val gruposMapeados = gruposDaApi.mapNotNull { grupoDto ->
                val id = grupoDto.id
                val nome = grupoDto.nome

                if (id == null || nome == null || nome.isBlank()) { // Adicionada verificação de nome.isBlank()
                    Log.w("ApiGrupoBackend", "Filtrando GrupoDTO: id=$id, nome='$nome'. Motivo: id ou nome é nulo/branco.")
                    null
                } else {
                    Log.d("ApiGrupoBackend", "Mapeando GrupoDTO: id=$id, nome='$nome' para Grupo.")
                    Grupo(
                        id = id,
                        nome = nome,
                        membros = grupoDto.membros ?: emptyList()
                    )
                }
            }
            Log.d("ApiGrupoBackend", "Número de grupos após mapeamento e filtro: ${gruposMapeados.size}")
            return gruposMapeados

        } catch (e: Exception) {
            Log.e("ApiGrupoBackend", "Exceção em getGrupoApi:", e)
            // Relançar a exceção ou retornar uma lista vazia com erro,
            // dependendo de como o ViewModel deve tratar isso.
            // Por enquanto, vamos manter o comportamento anterior do ViewModel de capturar.
            throw e // Garante que o .catch no ViewModel ainda pegue isso.
        }
    }

    // ... restante da sua classe ...
    override suspend fun getGrupoByIdApi(id: String): Grupo? {
        // Adicionar logs aqui também se estiver depurando esta função
        return try {
            api.get(id.toLong())
        } catch (e: Exception) {
            Log.e("ApiGrupoBackend", "Erro em getGrupoByIdApi para id $id:", e)
            null
        }
    }

    override suspend fun addGrupoApi(grupo: Grupo) {
        try {
            api.add(grupo)
        } catch (e: Exception) {
            Log.e("ApiGrupoBackend", "Erro em addGrupoApi:", e)
            // Lidar com o erro, talvez relançar uma exceção específica
        }
    }

    override suspend fun updateGrupoApi(id: Long, grupo: Grupo): Boolean {
        return try {
            api.update(id, grupo)
            true
        } catch (e: Exception) {
            Log.e("ApiGrupoBackend", "Erro em updateGrupoApi para id $id:", e)
            false
        }
    }

    override suspend fun deleteGrupoApi(id: Long): Boolean {
        // A sua função deleteGrupo no ViewModel espera um String,
        // mas aqui você recebe um Long. Certifique-se da consistência
        // ou converta onde necessário. No ViewModel você já faz id.toLongOrNull().
        // No repository (interface Grupobackend) e na API (GrupoApi)
        // deve estar esperando Long ou String consistentemente.
        return try {
            api.delete(id) // Assumindo que api.delete espera um Long
            true
        } catch (e: Exception) {
            Log.e("ApiGrupoBackend", "Erro em deleteGrupoApi para id $id:", e)
            false
        }
    }
}
