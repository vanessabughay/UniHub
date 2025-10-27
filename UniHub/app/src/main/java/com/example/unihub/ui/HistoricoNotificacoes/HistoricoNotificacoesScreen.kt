package com.example.unihub.ui.HistoricoNotificacoes

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.R
import kotlinx.coroutines.launch

@Composable
fun HistoricoNotificacoesScreen(
    onVoltar: () -> Unit
) {
    val context = LocalContext.current
    val application = remember(context) { context.applicationContext as Application }
    val factory = remember(application) { HistoricoNotificacoesViewModelFactory(application) }
    val viewModel: HistoricoNotificacoesViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val convitesProcessando by viewModel.convitesProcessando.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.mensagens.collect { mensagem ->
            Toast.makeText(context, mensagem, Toast.LENGTH_LONG).show()
        }
    }

    HistoricoNotificacoesScreenContent(
        uiState = uiState,
        convitesProcessando = convitesProcessando,
        onVoltar = onVoltar,
        onAcceptInvite = { conviteId -> scope.launch { viewModel.aceitarConvite(conviteId) } },
        onRejectInvite = { conviteId -> scope.launch { viewModel.rejeitarConvite(conviteId) } }
    )
}

@Composable
private fun HistoricoNotificacoesScreenContent(
    uiState: HistoricoNotificacoesUiState,
    convitesProcessando: Set<Long>,
    onVoltar: () -> Unit,
    onAcceptInvite: (Long) -> Unit,
    onRejectInvite: (Long) -> Unit
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
                        HistoricoNotificacaoCard(
                            notificacao = notificacao,
                            isProcessing = notificacao.shareInviteId?.let(convitesProcessando::contains) == true,
                            onAcceptInvite = onAcceptInvite,
                            onRejectInvite = onRejectInvite
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoricoNotificacaoCard(
    notificacao: HistoricoNotificacaoUiModel,
    isProcessing: Boolean,
    onAcceptInvite: (Long) -> Unit,
    onRejectInvite: (Long) -> Unit
) {
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
            if (notificacao.shareInviteId != null && notificacao.shareActionPending) {
                Spacer(modifier = Modifier.size(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onAcceptInvite(notificacao.shareInviteId) },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(text = stringResource(id = R.string.share_notification_action_accept))
                    }
                    OutlinedButton(
                        onClick = { onRejectInvite(notificacao.shareInviteId) },
                        enabled = !isProcessing
                    ) {
                        Text(text = stringResource(id = R.string.share_notification_action_reject))
                    }
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
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
                        id = 1L,
                        titulo = "Comentário em tarefa",
                        descricao = "Ana deixou um novo comentário na tarefa de Pesquisa de Mercado.",
                        dataHora = "12/05/2024 às 14:37",
                        shareInviteId = null,
                        shareActionPending = false
                    ),
                    HistoricoNotificacaoUiModel(
                        id = 2L,
                        titulo = "Convite de disciplina",
                        descricao = "João convidou você para compartilhar Física I.",
                        dataHora = "11/05/2024 às 18:00",
                        shareInviteId = 42L,
                        shareActionPending = true
                    )
                )
            ),
            convitesProcessando = emptySet(),
            onVoltar = {},
            onAcceptInvite = {},
            onRejectInvite = {}
        )
    }
}