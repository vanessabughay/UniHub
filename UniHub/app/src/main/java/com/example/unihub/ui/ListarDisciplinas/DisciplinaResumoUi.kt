package com.example.unihub.ui.ListarDisciplinas

import com.example.unihub.data.repository.DisciplinaResumo
import com.example.unihub.data.model.HorarioAula

// UI model separado da estrutura de dados original

data class DisciplinaResumoUi(
    val disciplinaId: String,
    val codigo: String,
    val nome: String,
    val horariosAulas: List<HorarioAula>
)

// Conversão do modelo de repositório para o modelo de UI
fun DisciplinaResumo.toUiModel(): DisciplinaResumoUi {
    return DisciplinaResumoUi(
        disciplinaId = this.disciplinaId.toString(),
        codigo = this.codigo,
        nome = this.nome,
        horariosAulas = this.aulas
    )
}
