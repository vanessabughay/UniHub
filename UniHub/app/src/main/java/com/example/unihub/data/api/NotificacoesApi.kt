package com.example.unihub.data.api

import com.example.unihub.data.model.NotificacoesConfig

interface NotificacoesApi {
    suspend fun carregar(): NotificacoesConfig
    suspend fun salvar(config: NotificacoesConfig)
}