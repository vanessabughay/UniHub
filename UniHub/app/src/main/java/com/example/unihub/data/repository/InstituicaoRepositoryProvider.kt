package com.example.unihub.data.repository


import android.content.Context


object InstituicaoRepositoryProvider {
    @Volatile
    private var repository: InstituicaoRepository? = null

    fun getRepository(context: Context): InstituicaoRepository =
        repository ?: synchronized(this) {
            repository ?: InstituicaoRepository(
                ApiInstituicaoBackend(),
                context.applicationContext
            ).also { repository = it }
        }
}