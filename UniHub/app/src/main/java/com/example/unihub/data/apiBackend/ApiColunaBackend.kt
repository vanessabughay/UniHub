package com.example.unihub.data.apiBackend

import com.example.unihub.data.api.ColunaApi
import com.example.unihub.data.config.RetrofitClient

object ApiColunaBackend {

    val apiService: ColunaApi by lazy {
        RetrofitClient.create(ColunaApi::class.java)    }
}