package com.example.unihub.ui.Notificacoes

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NotificacaoUiModel(
    val id: Long,
    val titulo: String,
    val descricao: String,
    val dataHora: String
)

data class NotificacoesUiState(
    val isLoading: Boolean = false,
    val notificacoes: List<NotificacaoUiModel> = emptyList()
)

class NotificacoesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotificacoesUiState(isLoading = true))
    val uiState: StateFlow<NotificacoesUiState> = _uiState.asStateFlow()

    init {
        carregarNotificacoes()
    }

    private fun carregarNotificacoes() {
        val notificacoesFicticias = listOf(
            NotificacaoUiModel(
                id = 1,
                titulo = "Comentário em tarefa",
                descricao = "Ana deixou um novo comentário na tarefa de Pesquisa de Mercado.",
                dataHora = "12/05/2024 às 14:37"
            ),
            NotificacaoUiModel(
                id = 2,
                titulo = "Prazo de avaliação",
                descricao = "A avaliação de Álgebra Linear vence amanhã às 10h.",
                dataHora = "11/05/2024 às 18:00"
            )
        )

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                notificacoes = notificacoesFicticias
            )
        }
    }
}