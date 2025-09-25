package com.example.unihub.data.repository

import com.example.unihub.data.api.RetrofitClient

object ApiQuadroBackend {



    val apiService: QuadroApi by lazy {
        RetrofitClient.create(QuadroApi::class.java)    }
}