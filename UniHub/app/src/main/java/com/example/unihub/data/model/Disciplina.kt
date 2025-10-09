package com.example.unihub.data.model

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
    val cargaHoraria: Int, // Tipo Int é melhor
    val aulas: List<HorarioAula>, // lista com as aulas que a disciplina tem, precisa por no diagrama
    val dataInicioSemestre: LocalDate,
    val dataFimSemestre: LocalDate, //ALTERAR TIPO

    // --- Informações do Professor ---
    val emailProfessor: String,
    val plataforma: String,
    val telefoneProfessor: String,
    val salaProfessor: String,

    //Campos não implementados, mas que devem ser corrigidos no diagrama
    //val faltas: Int   ERRADO! É um array de ausências

    //serão implementados em dataclass separados, mas são arrays do objeto DISCIPLINA
    //val ausencias: List<Ausencia>,
    val avaliacoes: List<Avaliacao>,

    //ativar/desativar notificações ou a disciplina
    val isAtiva: Boolean, //equivalente a situação
    val receberNotificacoes: Boolean //implementar no diagrama
)

//Construtor só com ID para Avaliacao
data class DisciplinaRef(
    val id: Long
)