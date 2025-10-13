package com.example.unihub.ui.VisualizarQuadro

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.data.model.Coluna
import com.example.unihub.data.model.Status
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.navigation.NavHostController

data class OpcaoQuadro(
    val title: String,
    val icon: ImageVector,
    val background: Color,
    val onClick: () -> Unit
)

@Composable
private fun OpcaoQuadroButton(item: OpcaoQuadro) {
    Button(
        onClick = item.onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = item.background,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        contentPadding = PaddingValues(vertical = 20.dp, horizontal = 16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(imageVector = item.icon, contentDescription = null, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = item.title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
        )
    }
}

private fun formatarPrazo(prazo: Long): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(prazo))
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizarQuadroScreen(
    navController: NavHostController,
    quadroId: String?,
    onVoltar: () -> Unit,
    onNavigateToEditQuadro: (String) -> Unit,
    onNavigateToNovaColuna: (String) -> Unit,
    onNavigateToEditarColuna: (String, String) -> Unit,
    onNavigateToNovaTarefa: (String, String) -> Unit,
    onNavigateToEditarTarefa: (String, String, String) -> Unit,
    viewModelFactory: VisualizarQuadroViewModelFactory
) {
    val viewModel: VisualizarQuadroViewModel = viewModel(factory = viewModelFactory)

    if (quadroId != null) {
        VisualizarQuadroContent(
            navController = navController,
            quadroId = quadroId,
            onVoltar = onVoltar,
            onNavigateToEditQuadro = onNavigateToEditQuadro,
            onNavigateToNovaColuna = onNavigateToNovaColuna,
            onNavigateToEditarColuna = onNavigateToEditarColuna,
            onNavigateToNovaTarefa = onNavigateToNovaTarefa,
            onNavigateToEditarTarefa = onNavigateToEditarTarefa,
            viewModel = viewModel
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Erro: ID do quadro não encontrado.")
        }
    }

}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VisualizarQuadroContent(
    navController: NavHostController,
    quadroId: String,
    onVoltar: () -> Unit,
    onNavigateToEditQuadro: (String) -> Unit,
    onNavigateToNovaColuna: (String) -> Unit,
    onNavigateToEditarColuna: (String, String) -> Unit,
    onNavigateToNovaTarefa: (String, String) -> Unit,
    onNavigateToEditarTarefa: (String, String, String) -> Unit,
    viewModel: VisualizarQuadroViewModel
) {

    val uiState by viewModel.uiState.collectAsState()

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val colunaAtualizada by savedStateHandle
        ?.getStateFlow("colunaAtualizada", false)
        ?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    var colunaExpandidaId by remember { mutableStateOf<String?>(null) }
    var secaoAtivaExpandida by remember { mutableStateOf(true) }
    var secaoConcluidaExpandida by remember { mutableStateOf(false) }

    val colunasAtivas = uiState.colunas
        .filter { it.status != Status.CONCLUIDA }
        .sortedBy { it.ordem }
    val colunasConcluidas = uiState.colunas
        .filter { it.status == Status.CONCLUIDA }
        .sortedBy { it.ordem }

    LaunchedEffect(quadroId) {
        viewModel.carregarQuadro(quadroId)
    }

    LaunchedEffect(colunaAtualizada) {
        if (colunaAtualizada) {
            viewModel.carregarQuadro(quadroId)
            savedStateHandle?.set("colunaAtualizada", false)
        }
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
                    onClick = { onNavigateToNovaColuna(quadroId) },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.tertiary,
                        contentColor = colorScheme.onTertiary
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
                onVoltar = onVoltar,
            )

            Spacer(modifier = Modifier.height(24.dp))

            val destinoId = uiState.quadro?.id ?: quadroId
            val tertiaryContainerColor = MaterialTheme.colorScheme.tertiary
            val opcoes = remember(destinoId, tertiaryContainerColor) {
                listOf(
                    OpcaoQuadro(
                        title = "Informações do quadro",
                        icon = Icons.Outlined.Info,
                        background = tertiaryContainerColor.copy(alpha = 0.4f),
                        onClick = { onNavigateToEditQuadro(destinoId) }
                    )
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (opcao in opcoes) {
                    OpcaoQuadroButton(opcao)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))


            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 50.dp))
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    if (colunasAtivas.isNotEmpty()) {
                        TituloDeSecao(
                            titulo = "Colunas em andamento",
                            setaAbaixo = secaoAtivaExpandida,
                            onClick = { secaoAtivaExpandida = !secaoAtivaExpandida }
                        )
                        AnimatedVisibility(visible = secaoAtivaExpandida) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                items(colunasAtivas, key = { it.id }) { coluna ->
                                    ColunaCard(
                                        coluna = coluna,
                                        isExpanded = colunaExpandidaId == coluna.id,
                                        onExpandToggle = {
                                            colunaExpandidaId = if (colunaExpandidaId == coluna.id) null else coluna.id
                                        },
                                        onEditColuna = { onNavigateToEditarColuna(quadroId, coluna.id) },
                                        onEditTarefa = { tarefaId -> onNavigateToEditarTarefa(quadroId, coluna.id, tarefaId) },
                                        onNewTarefa = { onNavigateToNovaTarefa(quadroId, coluna.id) }
                                    )
                                }
                            }
                        }
                    }

                    if (colunasConcluidas.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TituloDeSecao(
                            titulo = "Colunas concluídas",
                            setaAbaixo = secaoConcluidaExpandida,
                            onClick = { secaoConcluidaExpandida = !secaoConcluidaExpandida }
                        )
                        AnimatedVisibility(visible = secaoConcluidaExpandida) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                items(colunasConcluidas, key = { it.id }) { coluna ->
                                    ColunaCard(
                                        coluna = coluna,
                                        isExpanded = colunaExpandidaId == coluna.id,
                                        onExpandToggle = {
                                            colunaExpandidaId = if (colunaExpandidaId == coluna.id) null else coluna.id
                                        },
                                        onEditColuna = { onNavigateToEditarColuna(quadroId, coluna.id) },
                                        onEditTarefa = { tarefaId -> onNavigateToEditarTarefa(quadroId, coluna.id, tarefaId) },
                                        onNewTarefa = { onNavigateToNovaTarefa(quadroId, coluna.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


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
                tint = colorScheme.onSurface
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = titulo,
                color = colorScheme.onSurface,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 10.dp),
            thickness = DividerDefaults.Thickness,
            color = colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}


@Composable
private fun ColunaCard(
    coluna: Coluna,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onEditColuna: () -> Unit,
    onEditTarefa: (tarefaId: String) -> Unit,
    onNewTarefa: () -> Unit
) {
    val cardColor = colorScheme.tertiary.copy(alpha = 0.1f)
    val contentColor = colorScheme.onSurface

    Box(
        modifier = Modifier
            .width(320.dp)
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
                        Text("Editar Coluna",
                            color = MaterialTheme.colorScheme.tertiary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onNewTarefa,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.tertiary,
                            contentColor = colorScheme.onTertiary
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
private fun HeaderSection(titulo: String, onVoltar: () -> Unit) {
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
    }
}