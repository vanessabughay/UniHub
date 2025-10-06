package com.example.unihub.ui.VisualizarQuadro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.QuadroRepository
import com.example.unihub.data.repository._quadrobackend
import com.example.unihub.data.model.Quadro
import com.example.unihub.data.model.Estado
import com.example.unihub.data.model.Coluna
import com.example.unihub.data.model.Tarefa
import com.example.unihub.data.model.Status
import com.example.unihub.data.model.Priority
import androidx.compose.material.icons.filled.Info

private fun formatarPrazo(prazo: Long): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(prazo))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizarQuadroScreen(
    navController: NavHostController,
    quadroId: String,
    viewModelFactory: ViewModelProvider.Factory
) {
    val viewModel: VisualizarQuadroViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    var colunaExpandidaId by remember { mutableStateOf<String?>(null) }
    var secaoAtivaExpandida by remember { mutableStateOf(true) }
    var secaoConcluidaExpandida by remember { mutableStateOf(false) }

    val colunasAtivas = uiState.colunas.filter { it.status != Status.CONCLUIDA }
    val colunasConcluidas = uiState.colunas.filter { it.status == Status.CONCLUIDA }

    LaunchedEffect(quadroId) {
        viewModel.carregarQuadro(quadroId)
    }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { navController.navigate("colunaForm/$quadroId/new") },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text("Nova Coluna", modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            HeaderSection(
                titulo = uiState.quadro?.nome ?: "Carregando...",
                onVoltar = { navController.popBackStack() },
                onClickIconeDireita = { navController.navigate("quadroForm/$quadroId") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 50.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (colunasAtivas.isNotEmpty()) {
                        item {
                            TituloDeSecao(
                                titulo = "Colunas em andamento",
                                setaAbaixo = secaoAtivaExpandida,
                                onClick = { secaoAtivaExpandida = !secaoAtivaExpandida }
                            )
                        }
                        items(colunasAtivas, key = { it.id }) { coluna ->
                            AnimatedVisibility(visible = secaoAtivaExpandida) {
                                ColunaCard(
                                    coluna = coluna,
                                    isExpanded = colunaExpandidaId == coluna.id,
                                    onExpandToggle = {
                                        colunaExpandidaId = if (colunaExpandidaId == coluna.id) null else coluna.id
                                    },
                                    onEditColuna = { navController.navigate("colunaForm/$quadroId/${coluna.id}") },
                                    onEditTarefa = { tarefaId -> navController.navigate("tarefaForm/${coluna.id}/$tarefaId") },
                                    onNewTarefa = { navController.navigate("tarefaForm/${coluna.id}/new") }
                                )
                            }
                        }
                    }

                    if (colunasConcluidas.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            TituloDeSecao(
                                titulo = "Colunas concluídas",
                                setaAbaixo = secaoConcluidaExpandida,
                                onClick = { secaoConcluidaExpandida = !secaoConcluidaExpandida }
                            )
                        }
                        items(colunasConcluidas, key = { it.id }) { coluna ->
                            AnimatedVisibility(visible = secaoConcluidaExpandida) {
                                ColunaCard(
                                    coluna = coluna,
                                    isExpanded = colunaExpandidaId == coluna.id,
                                    onExpandToggle = {
                                        colunaExpandidaId = if (colunaExpandidaId == coluna.id) null else coluna.id
                                    },
                                    onEditColuna = { navController.navigate("colunaForm/$quadroId/${coluna.id}") },
                                    onEditTarefa = { tarefaId -> navController.navigate("tarefaForm/${coluna.id}/$tarefaId") },
                                    onNewTarefa = { navController.navigate("tarefaForm/${coluna.id}/new") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Composable para o título de seção, adaptado do seu modelo
@Composable
private fun TituloDeSecao(titulo: String, setaAbaixo: Boolean, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (setaAbaixo) Icons.Outlined.ExpandMore else Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = titulo,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Divider(
            modifier = Modifier.padding(top = 10.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

// ColunaCard é agora expansível e mostra Tarefas
@Composable
private fun ColunaCard(
    coluna: Coluna,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onEditColuna: () -> Unit,
    onEditTarefa: (tarefaId: String) -> Unit,
    onNewTarefa: () -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
    val contentColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(cardColor)
            .clickable(onClick = onExpandToggle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Expandir/Recolher",
                    tint = contentColor.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = coluna.titulo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )

                if (!isExpanded) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tarefas: ${coluna.tarefas.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    coluna.tarefas.forEach { tarefa ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { onEditTarefa(tarefa.id) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = tarefa.status == Status.CONCLUIDA,
                                onCheckedChange = { isChecked ->
                                    // AÇÃO DE ATUALIZAR STATUS DA TAREFA AQUI
                                }
                            )
                            Text(
                                text = tarefa.titulo,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = if (tarefa.status == Status.CONCLUIDA) TextDecoration.LineThrough else null
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatarPrazo(tarefa.prazo),
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onEditColuna) {
                        Text("Editar Coluna")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onNewTarefa,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    ) {
                        Text("Nova Tarefa")
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(titulo: String, onVoltar: () -> Unit, onClickIconeDireita: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Ícone de voltar
        IconButton(
            onClick = onVoltar,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.align(Alignment.Center)
        )

        IconButton(
            onClick = onClickIconeDireita,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Informações do Quadro",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}















