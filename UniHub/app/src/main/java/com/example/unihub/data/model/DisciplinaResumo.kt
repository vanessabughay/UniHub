package com.example.unihub.data.model

data class DisciplinaResumo(
    val id: String,
    val nome: String,
    val aulas: List<HorarioAula>
)
