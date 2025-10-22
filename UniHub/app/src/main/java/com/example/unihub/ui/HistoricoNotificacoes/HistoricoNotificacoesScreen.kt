package com.example.unihub.ui.HistoricoNotificacoes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.components.CabecalhoAlternativo

@Composable
fun HistoricoNotificacoesScreen(
    onVoltar: () -> Unit,
    viewModel: HistoricoNotificacoesViewModel = viewModel(factory = HistoricoNotificacoesViewModelFactory)
) {
    val uiState by viewModel.uiState.collectAsState()

    HistoricoNotificacoesScreenContent(
        uiState = uiState,
        onVoltar = onVoltar
    )
}

@Composable
private fun HistoricoNotificacoesScreenContent(
    uiState: HistoricoNotificacoesUiState,
    onVoltar: () -> Unit
) {
    Scaffold(
        topBar = {
            CabecalhoAlternativo(
                titulo = "Histórico de Notificações",
                onVoltar = onVoltar
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.notificacoes.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma notificação encontrada.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.notificacoes) { notificacao ->
                        HistoricoNotificacaoCard(notificacao = notificacao)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoricoNotificacaoCard(notificacao: HistoricoNotificacaoUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = notificacao.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = notificacao.descricao,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = notificacao.dataHora,
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Histórico de notificações"
)
@Composable
fun HistoricoNotificacoesScreenPreview() {
    MaterialTheme {
        HistoricoNotificacoesScreenContent(
            uiState = HistoricoNotificacoesUiState(
                notificacoes = listOf(
                    HistoricoNotificacaoUiModel(
                        id = 1,
                        titulo = "Comentário em tarefa",
                        descricao = "Ana deixou um novo comentário na tarefa de Pesquisa de Mercado.",
                        dataHora = "12/05/2024 às 14:37"
                    ),
                    HistoricoNotificacaoUiModel(
                        id = 2,
                        titulo = "Prazo de avaliação",
                        descricao = "A avaliação de Álgebra Linear vence amanhã às 10h.",
                        dataHora = "11/05/2024 às 18:00"
                    )
                )
            ),
            onVoltar = {}
        )
    }
}