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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.data.model.Coluna
import com.example.unihub.data.model.Status
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.example.unihub.data.model.Tarefa
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.ui.Shared.ZeroInsets
import com.example.unihub.data.config.TokenManager



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

private fun formatarPrazo(prazo: String?): String {
    val zonedDateTime = parseDeadline(prazo)
        ?: return "Sem prazo definido"
    val locale = Locale("pt", "BR")
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", locale)
    return formatter.format(zonedDateTime)
}

private fun parseDeadline(
    value: String?,
    zoneId: java.time.ZoneId = java.time.ZoneId.systemDefault()
): java.time.ZonedDateTime? {
    val trimmed = value?.trim().orEmpty()
    if (trimmed.isEmpty()) return null

    runCatching { java.time.Instant.parse(trimmed) }.getOrNull()?.let {
        return it.atZone(zoneId)
    }

    runCatching { java.time.ZonedDateTime.parse(trimmed) }.getOrNull()?.let { return it }
    runCatching { java.time.OffsetDateTime.parse(trimmed) }.getOrNull()?.let {
        return it.atZoneSameInstant(zoneId)
    }

    val localDateTimeFormatters = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    )

    localDateTimeFormatters.forEach { formatter ->
        runCatching { java.time.LocalDateTime.parse(trimmed, formatter) }
            .getOrNull()
            ?.let { return it.atZone(zoneId) }
    }

    runCatching { java.time.LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE) }
        .getOrNull()
        ?.let { return it.atStartOfDay(zoneId) }

    return null
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

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var usuarioLogadoId by remember {
        mutableStateOf<Long?>(TokenManager.usuarioId)
    }

    LaunchedEffect(Unit) {
        if (usuarioLogadoId == null) {
            TokenManager.loadToken(context.applicationContext)
            usuarioLogadoId = TokenManager.usuarioId
        }
    }



    LaunchedEffect(uiState.error) {
        uiState.error?.let { mensagem ->
            Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    var isFirstResume by remember(quadroId) { mutableStateOf(true) }

    LaunchedEffect(quadroId) {
        viewModel.carregarQuadro(quadroId)
    }

    DisposableEffect(lifecycleOwner, quadroId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (isFirstResume) {
                    isFirstResume = false
                } else {
                    viewModel.carregarQuadro(quadroId)
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var secaoAtivaExpandida by remember { mutableStateOf(true) }
    var secaoConcluidaExpandida by remember { mutableStateOf(false)}
    var mostrarSomenteResponsavel by rememberSaveable(quadroId) { mutableStateOf(false) }

    val colunasAtivas = uiState.colunas
        .filter { it.status != Status.CONCLUIDA }
        .sortedBy { it.ordem }
    val colunasConcluidas = uiState.colunas
        .filter { it.status == Status.CONCLUIDA }
        .sortedBy { it.ordem }

    val scrollState = rememberScrollState()



    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { onNavigateToNovaColuna(quadroId) },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.tertiary,
                        contentColor = colorScheme.onTertiary
                    )
                ) {
                    Text("Nova Coluna", modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        },
        contentWindowInsets = ZeroInsets
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            CabecalhoAlternativo(
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Checkbox(
                    checked = mostrarSomenteResponsavel,
                    onCheckedChange = { checked ->
                        if (usuarioLogadoId != null) {
                            mostrarSomenteResponsavel = checked
                        }
                    },
                    enabled = usuarioLogadoId != null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mostrar apenas minhas tarefas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 50.dp))
            } else {
                Column {
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
                                        mostrarSomenteResponsavel = mostrarSomenteResponsavel && usuarioLogadoId != null,
                                        usuarioLogadoId = usuarioLogadoId,
                                        onEditColuna = { onNavigateToEditarColuna(quadroId, coluna.id) },
                                        onEditTarefa = { tarefaId -> onNavigateToEditarTarefa(quadroId, coluna.id, tarefaId) },
                                        onNewTarefa = { onNavigateToNovaTarefa(quadroId, coluna.id) },
                                        onTarefaStatusChange = { tarefa, isChecked ->
                                            viewModel.atualizarStatusTarefa(quadroId, coluna.id, tarefa, isChecked)
                                        }
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
                                        mostrarSomenteResponsavel = mostrarSomenteResponsavel && usuarioLogadoId != null,
                                        usuarioLogadoId = usuarioLogadoId,
                                        onEditColuna = { onNavigateToEditarColuna(quadroId, coluna.id) },
                                        onEditTarefa = { tarefaId -> onNavigateToEditarTarefa(quadroId, coluna.id, tarefaId) },
                                        onNewTarefa = { onNavigateToNovaTarefa(quadroId, coluna.id) },
                                        onTarefaStatusChange = { tarefa, isChecked ->
                                            viewModel.atualizarStatusTarefa(quadroId, coluna.id, tarefa, isChecked)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
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
    mostrarSomenteResponsavel: Boolean,
    usuarioLogadoId: Long?,
    onEditColuna: () -> Unit,
    onEditTarefa: (tarefaId: String) -> Unit,
    onNewTarefa: () -> Unit,
    onTarefaStatusChange: (tarefa: Tarefa, isChecked: Boolean) -> Unit
) {
    val cardColor = colorScheme.tertiary.copy(alpha = 0.1f)
    val contentColor = colorScheme.onSurface
    val tarefasDaColuna = remember(coluna.tarefas, mostrarSomenteResponsavel, usuarioLogadoId) {
        if (mostrarSomenteResponsavel && usuarioLogadoId != null) {
            coluna.tarefas.filter { tarefa -> usuarioLogadoId in tarefa.responsaveisIds }
        } else {
            coluna.tarefas
        }
    }


    Box(
        modifier = Modifier
            .width(290.dp)
            .clip(MaterialTheme.shapes.large)
            .background(cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = coluna.titulo,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (coluna.status == Status.CONCLUIDA) "Concluída" else "Em andamento",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tarefas: ${tarefasDaColuna.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            val tarefasEmAndamento = tarefasDaColuna.filter { it.status != Status.CONCLUIDA }
            val tarefasConcluidas = tarefasDaColuna.filter { it.status == Status.CONCLUIDA }


            var andamentoExpandido by rememberSaveable(coluna.id, "andamento") { mutableStateOf(true) }
            var concluidasExpandidas by rememberSaveable(coluna.id, "concluidas") { mutableStateOf(false) }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (tarefasEmAndamento.isNotEmpty()) {
                    TarefasSection(
                        titulo = "Tarefas em andamento",
                        tarefas = tarefasEmAndamento,
                        contentColor = contentColor,
                        isExpanded = andamentoExpandido,
                        onToggleExpanded = { andamentoExpandido = !andamentoExpandido },
                        onEditTarefa = onEditTarefa,
                        onTarefaStatusChange = onTarefaStatusChange
                    )
                }

                if (tarefasConcluidas.isNotEmpty()) {
                    TarefasSection(
                        titulo = "Tarefas concluídas",
                        tarefas = tarefasConcluidas,
                        contentColor = contentColor,
                        isExpanded = concluidasExpandidas,
                        onToggleExpanded = { concluidasExpandidas = !concluidasExpandidas },
                        onEditTarefa = onEditTarefa,
                        onTarefaStatusChange = onTarefaStatusChange
                    )
                }

                if (tarefasEmAndamento.isEmpty() && tarefasConcluidas.isEmpty()) {
                    Text(
                        text = if (mostrarSomenteResponsavel) "Nenhuma tarefa atribuída a você" else "Nenhuma tarefa cadastrada",
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
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

@Composable
private fun TarefasSection(
    titulo: String,
    tarefas: List<Tarefa>,
    contentColor: Color,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onEditTarefa: (String) -> Unit,
    onTarefaStatusChange: (tarefa: Tarefa, isChecked: Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpanded),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Outlined.ExpandMore else Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = tarefas.size.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.6f)
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tarefas.forEach { tarefa ->
                    TarefaItem(
                        tarefa = tarefa,
                        contentColor = contentColor,
                        onEditTarefa = onEditTarefa,
                        onTarefaStatusChange = onTarefaStatusChange
                    )
                }
            }
        }
    }
}

@Composable
private fun TarefaItem(
    tarefa: Tarefa,
    contentColor: Color,
    onEditTarefa: (String) -> Unit,
    onTarefaStatusChange: (tarefa: Tarefa, isChecked: Boolean) -> Unit
) {

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
                onTarefaStatusChange(tarefa, isChecked)
            },
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.tertiary)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = tarefa.titulo,
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDecoration = if (tarefa.status == Status.CONCLUIDA) TextDecoration.LineThrough else null
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatarPrazo(tarefa.prazo),
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
        Spacer(modifier = Modifier.width(8.dp)) // Espaço no final
    }
}
