import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.model.HorarioAula
import com.example.unihub.data.repository._disciplinabackend
import java.time.LocalDate
import com.example.unihub.data.repository.DisciplinaResumo


class FakeDisciplinaBackend : _disciplinabackend {
    private val disciplinas = mutableListOf(
        DisciplinaResumo(
            disciplinaId = 1L,
            codigo = "DS430",
            nome = "Engenharia de Software",
            aulas = listOf(
                HorarioAula("Segunda", "A01", "08:00", "10:00"),
                HorarioAula("Quarta", "A02", "14:00", "16:00")
            )
        ),
        DisciplinaResumo(
            disciplinaId = 2L,
            codigo = "DS431",
            nome = "Banco de Dados",
            aulas = listOf(
                HorarioAula("Terça", "B05", "19:00", "22:00")
            )
        )
    )

    override suspend fun getDisciplinasResumoApi(): List<DisciplinaResumo> = disciplinas.toList()

    override suspend fun getDisciplinaByIdApi(id: String): Disciplina? {
        val resumo = disciplinas.find { it.disciplinaId.toString() == id } ?: return null
        return Disciplina(
            disciplinaId = resumo.disciplinaId,
            codigo = resumo.codigo,
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
        disciplinas += DisciplinaResumo(
            disciplinaId = disciplina.disciplinaId,
            codigo = disciplina.codigo,
            nome = disciplina.nome,
            aulas = disciplina.aulas
        )
    }

    override suspend fun updateDisciplinaApi(disciplina: Disciplina): Boolean {
        val index = disciplinas.indexOfFirst { it.disciplinaId == disciplina.disciplinaId }
        if (index == -1) return false
        disciplinas[index] = DisciplinaResumo(
            disciplinaId = disciplina.disciplinaId,
            codigo = disciplina.codigo,
            nome = disciplina.nome,
            aulas = disciplina.aulas
        )
        return true
    }

    override suspend fun deleteDisciplinaApi(id: String): Boolean {
        return disciplinas.removeIf { it.disciplinaId.toString() == id }
    }
}
