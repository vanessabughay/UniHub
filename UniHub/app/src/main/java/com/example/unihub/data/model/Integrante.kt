package com.example.unihub.data.model

/**
 * Esta é a nossa regra principal, chamada "Integrante".
 */
sealed interface Integrante {
    val id: Long?
    val nome: String?
}