package com.example.unihub.data.model

import java.time.LocalDate

/**
 * Representa uma ausência em determinada disciplina.
 */
data class Ausencia(
    val id: Long? = null,
    val disciplinaId: Long,
    val data: LocalDate,
    val justificativa: String? = null,
    val categoria: String? = null
)