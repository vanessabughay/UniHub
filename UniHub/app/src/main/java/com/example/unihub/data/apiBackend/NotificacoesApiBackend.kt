package com.example.unihub.data.apiBackend

import com.example.unihub.data.api.NotificacoesApi
import com.example.unihub.data.model.NotificacoesConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NotificacoesApiBackend : NotificacoesApi {

    private val mutex = Mutex()
    private var cache: NotificacoesConfig = NotificacoesConfig()

    override suspend fun carregar(): NotificacoesConfig {
        delay(350) // simula rede
        return mutex.withLock { cache }
    }


    override suspend fun salvar(
        config: NotificacoesConfig
    ): NotificacoesConfig {
        delay(350)
        mutex.withLock {
            cache = config
        }
        return config
    }
}