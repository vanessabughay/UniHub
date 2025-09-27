package com.example.unihub.data.apiBackend

import com.example.unihub.data.config.RetrofitClient
import com.example.unihub.data.api.TarefaApi

object ApiTarefaBackend {

    val apiService: TarefaApi by lazy {
        RetrofitClient.create(TarefaApi::class.java)    }
}