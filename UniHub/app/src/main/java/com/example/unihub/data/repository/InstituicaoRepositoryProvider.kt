package com.example.unihub.data.repository

object InstituicaoRepositoryProvider {
    val repository: InstituicaoRepository by lazy {
        InstituicaoRepository(ApiInstituicaoBackend())
    }
}