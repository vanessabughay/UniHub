package com.example.unihub.data.repository

import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.model.HorarioAula
import java.time.LocalDate

/**
 * Simple in-memory backend used while the real API integration is pending.
 */
class FakeDisciplinaBackend : _disciplinabackend {
    private val disciplinas = mutableListOf(
        DisciplinaResumo(
            id = "DS430",
            nome = "Engenharia de Software",
            aulas = listOf(
                HorarioAula(
                    diaDaSemana = "Segunda",
                    sala = "A01",
                    horarioInicio = "08:00",
                    horarioFim = "10:00"
                ),
                HorarioAula(
                    diaDaSemana = "Quarta",
                    sala = "A02",
                    horarioInicio = "14:00",
                    horarioFim = "16:00"
                )
            )
        ),
        DisciplinaResumo(
            id = "DS431",
            nome = "Banco de Dados",
            aulas = listOf(
                HorarioAula(
                    diaDaSemana = "Terça",
                    sala = "B05",
                    horarioInicio = "19:00",
                    horarioFim = "22:00"
                )
            )
        )
    )

    override suspend fun getDisciplinasResumoApi(): List<DisciplinaResumo> = disciplinas

    override suspend fun getDisciplinaByIdApi(id: String): Disciplina? {
        // For the demo only basic mapping from resumo to full object
        val resumo = disciplinas.find { it.id == id } ?: return null
        return Disciplina(
            id = resumo.id,
            nome = resumo.nome,
            professor = "",
            periodo = "",
            cargaHoraria = 0,
            aulas = resumo.aulas,
            dataInicioSemestre = LocalDate.now(),
            dataFimSemestre = LocalDate.now().plusMonths(6),
            emailProfessor = "",
            plataforma = "",
            telefoneProfessor = "",
            salaProfessor = "",
            isAtiva = true,
            receberNotificacoes = true
        )
    }

    override suspend fun addDisciplinaApi(disciplina: Disciplina) {
        disciplinas += DisciplinaResumo(disciplina.id, disciplina.nome, disciplina.aulas)
    }

    override suspend fun updateDisciplinaApi(disciplina: Disciplina): Boolean {
        val index = disciplinas.indexOfFirst { it.id == disciplina.id }
        if (index == -1) return false
        disciplinas[index] = DisciplinaResumo(disciplina.id, disciplina.nome, disciplina.aulas)
        return true
    }

    override suspend fun deleteDisciplinaApi(id: String): Boolean {
        return disciplinas.removeIf { it.id == id }
    }
}