package com.example.unihub

data class Disciplina(
    val codigo: String,
    val nome: String,
    val dia: String,
    val sala: String,
    val horario: String,
    val selecionada: Boolean = false
)
