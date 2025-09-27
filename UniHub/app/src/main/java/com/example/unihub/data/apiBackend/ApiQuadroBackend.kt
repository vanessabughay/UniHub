package com.example.unihub.data.apiBackend

import com.example.unihub.data.api.QuadroApi
import com.example.unihub.data.config.RetrofitClient

object ApiQuadroBackend {



    val apiService: QuadroApi by lazy {
        RetrofitClient.create(QuadroApi::class.java)    }
}