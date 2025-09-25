package com.example.unihub.data.repository

import com.example.unihub.data.api.RetrofitClient

object ApiColunaBackend {

    val apiService: ColunaApi by lazy {
        RetrofitClient.create(ColunaApi::class.java)    }
}