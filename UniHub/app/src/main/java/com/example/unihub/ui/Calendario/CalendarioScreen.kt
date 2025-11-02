package com.example.unihub.ui.Calendario

import android.os.Build
import androidx.annotation.RequiresExtension
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.data.model.Avaliacao
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun CalendarioRoute(
    viewModel: CalendarioViewModel,
    onNovaAvaliacao: () -> Unit,
    onAvaliacaoClick: (Long) -> Unit,
    onVoltar: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CalendarioScreen(
        state = state,
        onNovaAvaliacao = onNovaAvaliacao,
        onAvaliacaoClick = onAvaliacaoClick,
        onVoltar = onVoltar,
        onMesAnterior = viewModel::irParaMesAnterior,
        onMesProximo = viewModel::irParaProximoMes,
        onAlterarVisualizacao = viewModel::alterarVisualizacao,
        onSelecionarMesAno = viewModel::irParaMes
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
    onSelecionarMesAno: (YearMonth) -> Unit
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
        containerColor = Color(0xFFF6F7FB)
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
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

            BotaoNovaAvaliacao(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = onNovaAvaliacao
            )
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
            // Container das Semanas (estica para preencher)
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
                                .background(if (mes == mesSelecionado) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
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
                                .background(if (ano == anoSelecionado) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
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
        modifier = modifier.height(44.dp).navigationBarsPadding(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF111827)
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp,
            brush = SolidColor(Color(0xFFD1D5DB))
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
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