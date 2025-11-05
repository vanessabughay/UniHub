package com.example.unihub.data.model

enum class Antecedencia(val dias: Int, val label: String) {
    NO_DIA(0, "No dia"),
    UM_DIA(1, "1 dia antes"),
    DOIS_DIAS(2, "2 dias antes"),
    TRES_DIAS(3, "3 dias antes"),
    UMA_SEMANA(7, "1 semana antes");
    companion object {
        val padrao = NO_DIA
        val todas = listOf(NO_DIA, UM_DIA, DOIS_DIAS, TRES_DIAS, UMA_SEMANA)
    }
}