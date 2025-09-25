package com.example.unihub.data.repository

import com.example.unihub.data.api.RetrofitClient

object ApiTarefaBackend {

    val apiService: TarefaApi by lazy {
        RetrofitClient.create(TarefaApi::class.java)    }
}