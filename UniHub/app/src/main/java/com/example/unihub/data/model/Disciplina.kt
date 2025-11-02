package com.example.unihub.data.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

// HorarioAula é uma classe simples para representar um horário de aula em um dia específico.
data class HorarioAula(
    val diaDaSemana: String,
    val sala: String,
    val horarioInicio: Int,
    val horarioFim: Int
)

data class Disciplina(
    // --- Informações Gerais ---
    val id: Long?=null,
    val codigo: String,
    val nome: String? = null,
    val professor: String,
    val periodo: String,

    // --- Informações de Aula ---
    val cargaHoraria: Int? = null, // Tipo Int é melhor
    val qtdSemanas: Int? = null,
    val aulas: List<HorarioAula>, // lista com as aulas que a disciplina tem, precisa por no diagrama
    val dataInicioSemestre: LocalDate,
    val dataFimSemestre: LocalDate, //ALTERAR TIPO

    // --- Informações do Professor ---
    val emailProfessor: String,
    val plataforma: String,
    val telefoneProfessor: String,
    val salaProfessor: String,

    val ausencias: List<Ausencia> = emptyList(),
    val ausenciasPermitidas: Int? = null,
    val avaliacoes: List<Avaliacao>,

    //ativar/desativar notificações ou a disciplina
    @SerializedName("ativa") // <-- Diz ao Retrofit/Gson para mapear a chave "ativa" do JSON
    val isAtiva: Boolean, //equivalente a situação
    val receberNotificacoes: Boolean //implementar no diagrama
)

//Construtor só com ID para Avaliacao
data class DisciplinaRef(
    val id: Long
)