package com.example.unihub.ui.ManterQuadro

import com.example.unihub.data.model.Estado
import com.example.unihub.data.model.Quadro

data class DisciplinaResumoUi(val id: Long?, val nome: String?)
data class ContatoResumoUi(val id: Long?, val nome: String?)
data class GrupoResumoUi(val id: Long?, val nome: String?)


sealed interface IntegranteUi {
    val id: Long?
    val nome: String?
}
data class ContatoIntegranteUi(override val id: Long?, override val nome: String?) : IntegranteUi
data class GrupoIntegranteUi(override val id: Long?, override val nome: String?) : IntegranteUi

data class QuadroFormUiState(
    val nome: String = "",
    val estado: Estado = Estado.ATIVO,
    val prazo: Long = System.currentTimeMillis(),
    val disciplinaSelecionada: DisciplinaResumoUi? = null,
    val integranteSelecionado: IntegranteUi? = null,
    val disciplinasDisponiveis: List<DisciplinaResumoUi> = emptyList(),
    val contatosDisponiveis: List<ContatoResumoUi> = emptyList(),
    val gruposDisponiveis: List<GrupoResumoUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val quadroSendoEditado: Quadro? = null,
    val isSelectionDialogVisible: Boolean = false
)