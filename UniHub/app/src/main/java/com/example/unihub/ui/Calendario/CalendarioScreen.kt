package com.example.unihub.ui.Calendario

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresExtension
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.data.model.Avaliacao
import com.example.unihub.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs
import com.example.unihub.ui.Shared.ZeroInsets


@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun CalendarioRoute(
    viewModel: CalendarioViewModel,
    onNovaAvaliacao: () -> Unit,
    onAvaliacaoClick: (Long) -> Unit,
    onVoltar: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val calendarSignInOptions = remember(context) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestServerAuthCode(context.getString(R.string.server_client_id), true)
            .requestScopes(
                Scope("https://www.googleapis.com/auth/calendar.events"),
                Scope("https://www.googleapis.com/auth/calendar.events.readonly")
            )
            .build()
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val authCode = account?.serverAuthCode
            if (!authCode.isNullOrBlank()) {
                viewModel.linkGoogleCalendar(authCode)
            } else {
                viewModel.reportCalendarError("Não foi possível obter autorização do Google.")
            }
        } catch (e: ApiException) {
            viewModel.reportCalendarError("Falha no Google Sign-In: ${e.statusCode}")
        } finally {
            GoogleSignIn.getClient(context, calendarSignInOptions).signOut()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshCalendarStatus()
    }

    val startLinkFlow = remember(context, calendarSignInOptions) {
        {
            val client = GoogleSignIn.getClient(context, calendarSignInOptions)
            launcher.launch(client.signInIntent)
        }
    }

    CalendarioScreen(
        state = state,
        onNovaAvaliacao = onNovaAvaliacao,
        onAvaliacaoClick = onAvaliacaoClick,
        onVoltar = onVoltar,
        onMesAnterior = viewModel::irParaMesAnterior,
        onMesProximo = viewModel::irParaProximoMes,
        onAlterarVisualizacao = viewModel::alterarVisualizacao,
        onSelecionarMesAno = viewModel::irParaMes,
        onLinkGoogleCalendar = startLinkFlow,
        onUnlinkGoogleCalendar = viewModel::unlinkGoogleCalendar,
        onSyncGoogleCalendar = viewModel::syncGoogleCalendar,
        onDismissCalendarMessage = viewModel::clearCalendarMessages
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreen(
    state: CalendarioUiState,
    onNovaAvaliacao: () -> Unit,
    onAvaliacaoClick: (Long) -> Unit,
    onVoltar: () -> Unit,
    onMesAnterior: () -> Unit,
    onMesProximo: () -> Unit,
    onAlterarVisualizacao: () -> Unit,
    onSelecionarMesAno: (YearMonth) -> Unit,
    onLinkGoogleCalendar: () -> Unit,
    onUnlinkGoogleCalendar: () -> Unit,
    onSyncGoogleCalendar: () -> Unit,
    onDismissCalendarMessage: () -> Unit
) {
    var showMonthPicker by remember { mutableStateOf(false) }
    var chipEmDetalhe by remember { mutableStateOf<AvaliacaoChipUi?>(null) }

    if (showMonthPicker) {
        val currentMonth = try {
            YearMonth.parse(state.titulo.lowercase(), DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale("pt", "BR")))
        } catch (e: Exception) {
            YearMonth.now()
        }
        SeletorDeMesAnoDialog(
            mesAtual = currentMonth,
            onDismiss = { showMonthPicker = false },
            onConfirm = {
                onSelecionarMesAno(it)
                showMonthPicker = false
            }
        )
    }

    if (chipEmDetalhe != null) {
        DetalheAvaliacaoDialog(
            chip = chipEmDetalhe!!,
            onDismiss = { chipEmDetalhe = null }
        )
    }

    Scaffold(
        topBar = {
            CabecalhoAlternativo(
                titulo = "Calendário",
                onVoltar = onVoltar
            )
        },

        bottomBar = {
            BotaoNovaAvaliacao(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(), // Adiciona padding para a barra de navegação
                onClick = onNovaAvaliacao
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = ZeroInsets
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            GoogleCalendarExpandableSection(
                state = state,
                onConnect = onLinkGoogleCalendar,
                onDisconnect = onUnlinkGoogleCalendar,
                onSync = onSyncGoogleCalendar,
                onDismissMessage = onDismissCalendarMessage,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ControleVisualizacaoEMes(
                state = state,
                onAlterarVisualizacao = onAlterarVisualizacao,
                onAbrirSeletorMes = { showMonthPicker = true }
            )

            var dragAmount by remember { mutableFloatStateOf(0f) }
            Box(

                modifier = Modifier
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { dragAmount = 0f },
                            onDragEnd = {
                                if (abs(dragAmount) > 50) {
                                    if (dragAmount > 0) onMesAnterior() else onMesProximo()
                                }
                            },
                            onDrag = { change, drag ->
                                dragAmount += drag.x
                                change.consume()
                            }
                        )
                    }
            ) {
                when (state.visualizacao) {
                    VisualizacaoCalendario.GRID -> CalendarioGridView(
                        state = state,
                        onAvaliacaoClick = onAvaliacaoClick,
                        onChipLongClick = { chip ->
                            chipEmDetalhe = chip
                        }
                    )
                    VisualizacaoCalendario.LISTA -> CalendarioListView(state, onAvaliacaoClick)
                }
            }

        }
    }
}

@Composable
private fun ControleVisualizacaoEMes(
    state: CalendarioUiState,
    onAlterarVisualizacao: () -> Unit,
    onAbrirSeletorMes: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onAlterarVisualizacao) {
            Icon(
                imageVector = if (state.visualizacao == VisualizacaoCalendario.GRID) {
                    Icons.Filled.ViewList
                } else {
                    Icons.Filled.CalendarViewMonth
                },
                contentDescription = "Alterar Visualizacao",
                tint = MaterialTheme.colorScheme.tertiary
            )
        }

        Text(
            text = state.titulo,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            textAlign = TextAlign.Start,
            modifier = Modifier
                .weight(1f)
                .clickable { onAbrirSeletorMes() }
                .padding(horizontal = 12.dp)
        )
    }
}


@Composable
private fun GoogleCalendarExpandableSection(
    state: CalendarioUiState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onSync: () -> Unit,
    onDismissMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = if (isExpanded)
                        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    else
                        RoundedCornerShape(12.dp)
                )
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Google Agenda",
                color = MaterialTheme.colorScheme.onTertiary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (isExpanded) "Recolher" else "Expandir",
                tint = MaterialTheme.colorScheme.onTertiary
            )
        }


        AnimatedVisibility(visible = isExpanded) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                // Forma se encaixa com o cabeçalho
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                // Conteúdo original do card foi movido para este Composable
                GoogleCalendarCardContent(
                    state = state,
                    onConnect = onConnect,
                    onDisconnect = onDisconnect,
                    onSync = onSync,
                    onDismissMessage = onDismissMessage
                )
            }
        }
    }
}


@Composable
private fun GoogleCalendarCardContent(
    state: CalendarioUiState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onSync: () -> Unit,
    onDismissMessage: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        if (state.calendarLinked) {
            Text(
                text = "Google Agenda conectado",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Última sincronização: ${state.calendarLastSyncedLabel ?: "nunca"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (state.calendarRequiresReauth) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "É necessário conceder acesso novamente para continuar sincronizando.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onSync,
                    enabled = !state.isCalendarSyncing && !state.isCalendarLinking,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (state.isCalendarSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Text("Sincronizar agora")
                }
                Spacer(modifier = Modifier.width(12.dp))
                TextButton(
                    onClick = onDisconnect,
                    enabled = !state.isCalendarSyncing && !state.isCalendarLinking
                ) {
                    Text("Desconectar")
                }
            }
        } else {
            Text(
                text = "Sincronize com o Google Agenda",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Permita que suas tarefas, atividades e provas apareçam automaticamente no Google Agenda.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onConnect,
                enabled = !state.isCalendarLinking,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (state.isCalendarLinking) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text("Conectar ao Google Agenda")
            }
        }

        if (state.calendarError != null || state.calendarMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            val isError = state.calendarError != null
            val message = state.calendarError ?: state.calendarMessage.orEmpty()
            Surface(
                color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismissMessage) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Fechar mensagem"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarioGridView(
    state: CalendarioUiState,
    onAvaliacaoClick: (Long) -> Unit,
    onChipLongClick: (AvaliacaoChipUi) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)) {

        //Cabeçalho dos dias
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom").forEach {
                Text(
                    text = it,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Divider(color = Color(0xFFE5E7EB))

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                val semanas = state.diasGrid.chunked(7)

                semanas.forEach { semana ->
                    // Linha da Semana
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // dia
                        semana.forEach { dia ->
                            DiaCell(
                                dataLabel = dia.data?.dayOfMonth?.toString(),
                                chips = dia.avaliacoes,
                                onAvaliacaoClick = onAvaliacaoClick,
                                onChipLongClick = onChipLongClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarioListView(state: CalendarioUiState, onAvaliacaoClick: (Long) -> Unit) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (state.avaliacoesDoMes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("Nenhuma avaliação para este mês.", fontSize = 16.sp, color = Color.Gray)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(state.avaliacoesDoMes, key = { it.id!! }) { avaliacao ->
                ItemAvaliacaoEmLista(avaliacao = avaliacao, onClick = { onAvaliacaoClick(avaliacao.id!!) })
            }
        }
    }
}

@Composable
private fun ItemAvaliacaoEmLista(avaliacao: Avaliacao, onClick: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM EEEE", Locale("pt", "BR"))
    val dataEntrega = remember(avaliacao.dataEntrega) {
        try {
            LocalDate.parse(avaliacao.dataEntrega?.substring(0, 10))
        } catch (e: Exception) {
            null
        }
    }
    val chipUi = remember(avaliacao) { avaliacao.toChipUi() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(chipUi?.color ?: Color.Gray, CircleShape)
            )
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = chipUi?.titulo ?: "Avaliação",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (chipUi != null && chipUi.subtitulo.isNotBlank()) {
                    Text(
                        text = chipUi.subtitulo,
                        fontSize = 15.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (dataEntrega != null) {
                    Text(
                        text = formatter.format(dataEntrega).replaceFirstChar { it.titlecase(Locale.getDefault()) },
                        fontSize = 14.sp,
                        color = Color.Gray.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SeletorDeMesAnoDialog(
    mesAtual: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit
) {
    var anoSelecionado by remember { mutableIntStateOf(mesAtual.year) }
    var mesSelecionado by remember { mutableIntStateOf(mesAtual.monthValue) }
    val anos = remember { (mesAtual.year - 5..mesAtual.year + 5).toList() }
    val meses = remember {
        (1..12).map { mes ->
            YearMonth.of(2024, mes).format(DateTimeFormatter.ofPattern("MMMM", Locale("pt", "BR"))).replaceFirstChar { it.titlecase() }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecione o mês e o ano") },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                LazyColumn(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    items(meses.size) { index ->
                        val mes = index + 1
                        Text(
                            text = meses[index],
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { mesSelecionado = mes }
                                .background(if (mes == mesSelecionado) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f) else Color.Transparent)
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                LazyColumn(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    items(anos) { ano ->
                        Text(
                            text = ano.toString(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { anoSelecionado = ano }
                                .background(if (ano == anoSelecionado) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f) else Color.Transparent)
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(YearMonth.of(anoSelecionado, mesSelecionado)) }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun DiaCell(
    dataLabel: String?,
    chips: List<AvaliacaoChipUi>,
    onAvaliacaoClick: (Long) -> Unit,
    onChipLongClick: (AvaliacaoChipUi) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 4.dp)
    ) {
        if (dataLabel != null) {
            Text(
                text = dataLabel,
                color = Color(0xFF6B7280),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (dataLabel != null) {
                Spacer(Modifier.height(4.dp))
            }

            chips.forEach { chip ->
                EventoChip(
                    chip = chip,
                    onClick = { onAvaliacaoClick(chip.id) },
                    onLongClick = { onChipLongClick(chip) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EventoChip(
    chip: AvaliacaoChipUi,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .widthIn(min = 72.dp)
            .wrapContentWidth()
            .background(chip.color, RoundedCornerShape(10.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = chip.titulo,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 14.sp
        )
        if (chip.subtitulo.isNotBlank()) {
            Text(
                text = chip.subtitulo,
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
private fun BotaoNovaAvaliacao(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF111827)
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp,
            brush = SolidColor(Color(0xFFD1D5DB))
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),

        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 16.dp)
    ) {
        Text(
            text = "Nova avaliação",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DetalheAvaliacaoDialog(
    chip: AvaliacaoChipUi,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Detalhes da Avaliação") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = chip.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (chip.subtitulo.isNotBlank()) {
                    Text(
                        text = "Disciplina: ${chip.subtitulo}",
                        fontSize = 16.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}