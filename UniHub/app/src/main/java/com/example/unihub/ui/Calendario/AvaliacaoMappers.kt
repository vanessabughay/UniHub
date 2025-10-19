package com.example.unihub.ui.Calendario

import androidx.compose.ui.graphics.Color
import com.example.unihub.data.model.Avaliacao
import com.example.unihub.data.model.Prioridade

//criar um chip/card para a avaliaçao
fun Avaliacao.toChipUi(): AvaliacaoChipUi? {
    val idSafe = id ?: return null
    val tituloBase = when {
        !tipoAvaliacao.isNullOrBlank() -> tipoAvaliacao!!.trim()
        !descricao.isNullOrBlank() -> descricao!!.trim()
        else -> "Avaliação"
    }

    val subtituloBase = this.disciplina?.nome ?: ""

    val color = pickChipColor(tituloBase, this.prioridade)

    return AvaliacaoChipUi(
        id = idSafe,
        titulo = tituloBase,
        subtitulo = subtituloBase,
        color = color
    )
}

private fun pickChipColor(titulo: String, prioridade: Prioridade): Color {

    val lTitulo = titulo.lowercase()

    return when {
        // cor por tipo?
        "prova" in lTitulo -> Color(0xFF5B21B6)
        "redação" in lTitulo -> Color(0xFFF59E0B)
        "trabalho" in lTitulo -> Color(0xFFBA68C8)

        //cor por prioridade?
        else -> when (prioridade) {
            Prioridade.MUITO_ALTA -> Color(0xFFD32F2F)
            Prioridade.ALTA -> Color(0xFFF57C00)
            Prioridade.MEDIA -> Color(0xFF26A69A)
            Prioridade.BAIXA -> Color(0xFF3949AB)
            Prioridade.MUITO_BAIXA -> Color(0xFF607D8B)
        }
    }
}