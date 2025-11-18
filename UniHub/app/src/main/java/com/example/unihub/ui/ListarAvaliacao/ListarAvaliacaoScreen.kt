package com.example.unihub.ui.ListarAvaliacao

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresExtension
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.components.CampoBusca
import androidx.compose.material3.HorizontalDivider
import com.example.unihub.data.model.Antecedencia
import com.example.unihub.data.model.Avaliacao
import com.example.unihub.data.model.EstadoAvaliacao
import com.example.unihub.notifications.AvaliacaoNotificationScheduler
import com.example.unihub.ui.Shared.ZeroInsets

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.unihub.ui.Shared.NotaCampo


val CardDefaultBackgroundColor = Color(0xFFD4D4E8)
val LilasCard = Color(0xFFE0E1F8)
val LilasButton = Color(0xFF9799FF)

private fun formatarDataHora(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    val padrõesLdt = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    )
    for (fmt in padrõesLdt) {
        try {
            val ldt = LocalDateTime.parse(iso, fmt)
            return ldt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy (HH:mm)"))
        } catch (_: Exception) { }
    }
    return try {
        val ld = LocalDate.parse(iso)
        ld.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (_: Exception) {
        iso
    }
}


@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun ListarAvaliacaoScreen(
    viewModel: ListarAvaliacaoViewModel = viewModel(factory = ListarAvaliacaoViewModelFactory),
    onAddAvaliacaoParaDisciplina: (disciplinaId: String) -> Unit,
    onAddAvaliacaoGeral: () -> Unit,
    onVoltar: () -> Unit,
    onNavigateToManterAvaliacao: (avaliacaoId: String) -> Unit
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val context = LocalContext.current
    val avaliacoesState by viewModel.avaliacoes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val scheduler = remember { AvaliacaoNotificationScheduler(context.applicationContext) }
    val notificationPermissionLauncher = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }
    } else {
        null
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(avaliacoesState) {
        val infos = avaliacoesState.mapNotNull { avaliacao ->
            val id = avaliacao.id ?: return@mapNotNull null

            val disciplinaIdLong = (avaliacao.disciplina?.id as? String)?.toLongOrNull()

            AvaliacaoNotificationScheduler.AvaliacaoInfo(
                id = id,
                descricao = avaliacao.descricao ?: avaliacao.tipoAvaliacao,
                disciplinaId = disciplinaIdLong,
                disciplinaNome = avaliacao.disciplina?.nome,
                dataHoraIso = avaliacao.dataEntrega,
                reminderDuration = AvaliacaoNotificationScheduler.defaultReminderDuration(avaliacao.prioridade),

                // receberNotificacoes = avaliacao.receberNotificacoes,
                // antecedenciaDias = Antecedencia.padrao.dias
                receberNotificacoes = avaliacao.receberNotificacoes


            )
        }
        scheduler.scheduleNotifications(infos)
    }

    var showConfirmDeleteDialog by remember { mutableStateOf(false) }
    var avaliacaoParaExcluir by remember { mutableStateOf<Avaliacao?>(null) }

    var avaliacaoParaConcluir by remember { mutableStateOf<Avaliacao?>(null) }
    var avaliacaoParaReativar by remember { mutableStateOf<Avaliacao?>(null) }
    var isButtonEnabled by remember { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadAvaliacao()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val avaliacoesComIdValido by remember(avaliacoesState) {
        derivedStateOf { avaliacoesState.filter { it.id != null } }
    }

    val avaliacoesFiltrados by remember(searchQuery, avaliacoesComIdValido) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                avaliacoesComIdValido
            } else {
                val q = searchQuery.trim()
                avaliacoesComIdValido.filter { avaliacao ->
                    val descOk = avaliacao.descricao?.contains(q, ignoreCase = true) == true
                    val idOk   = avaliacao.id?.toString()?.contains(q, ignoreCase = true) == true
                    val discOk = avaliacao.disciplina?.nome?.contains(q, ignoreCase = true) == true
                    descOk || idOk || discOk
                }
            }
        }
    }

    if (showConfirmDeleteDialog && avaliacaoParaExcluir != null) {
        val av = avaliacaoParaExcluir!!
        AlertDialog(
            onDismissRequest = {
                showConfirmDeleteDialog = false
                avaliacaoParaExcluir = null
            },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Deseja mesmo excluir a avaliação \"${av.descricao}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAvaliacao(av.id!!.toString()) { sucesso ->
                            if (sucesso) Toast.makeText(context, "Avaliação excluída!", Toast.LENGTH_SHORT).show()
                        }
                        showConfirmDeleteDialog = false
                        avaliacaoParaExcluir = null
                    }
                ) { Text("EXCLUIR") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmDeleteDialog = false
                    avaliacaoParaExcluir = null
                }) { Text("CANCELAR") }
            }
        )
    }

    if (avaliacaoParaConcluir != null) {
        val av = avaliacaoParaConcluir!!
        AlertDialog(
            onDismissRequest = { avaliacaoParaConcluir = null },
            title = { Text("Confirmar") },
            text = { Text("Deseja mesmo concluir essa avaliação?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.toggleConcluida(av, true) { ok ->
                        if (ok) Toast.makeText(context, "Avaliação concluída!", Toast.LENGTH_SHORT).show()
                    }
                    avaliacaoParaConcluir = null
                }) { Text("CONCLUIR AVALIAÇÃO") }
            },
            dismissButton = {
                TextButton(onClick = { avaliacaoParaConcluir = null }) { Text("CANCELAR") }
            }
        )
    }

    if (avaliacaoParaReativar != null) {
        val av = avaliacaoParaReativar!!
        AlertDialog(
            onDismissRequest = { avaliacaoParaReativar = null },
            title = { Text("Confirmar") },
            text = { Text("Deseja mesmo reativar essa avaliação?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.toggleConcluida(av, false) { ok ->
                        if (ok) Toast.makeText(context, "Avaliação reativada!", Toast.LENGTH_SHORT).show()
                    }
                    avaliacaoParaReativar = null
                }) { Text("REATIVAR AVALIAÇÃO") }
            },
            dismissButton = {
                TextButton(onClick = { avaliacaoParaReativar = null }) { Text("CANCELAR") }
            }
        )
    }

    var notaDialogAvaliacao by remember { mutableStateOf<Avaliacao?>(null) }
    var notaTemp by remember { mutableStateOf("") }

    if (notaDialogAvaliacao != null) {
        val av = notaDialogAvaliacao!!
        AlertDialog(
            onDismissRequest = { notaDialogAvaliacao = null },
            title = { Text("Definir nota") },
            text = {
                Column {
                    Text("Avaliação: " + (av.descricao ?: ""))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = NotaCampo.formatFieldText(notaTemp),
                        onValueChange = { notaTemp = NotaCampo.sanitize(it) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Nota") },
                        placeholder = { Text("Ex.: 8,5") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isButtonEnabled) {
                            isButtonEnabled = false

                            val valor = NotaCampo.toDouble(notaTemp)
                            viewModel.updateNota(av, valor) { ok ->
                                isButtonEnabled = true

                                if (ok) {
                                    Toast.makeText(context, "Nota salva!", Toast.LENGTH_SHORT).show()
                                    notaDialogAvaliacao = null
                                } else {
                                    Toast.makeText(context, "Erro ao salvar nota!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    enabled = isButtonEnabled
                ) {
                    Text(if (isButtonEnabled) "SALVAR" else "Salvando...")
                }
            },
            dismissButton = {
                TextButton(onClick = { notaDialogAvaliacao = null }) { Text("CANCELAR") }
            }
        )
    }

    Scaffold(
        topBar = {
            CabecalhoAlternativo(
                titulo = "Avaliações",
                onVoltar = onVoltar,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAvaliacaoGeral,
                containerColor = LilasButton,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Avaliação")
            }
        },
        contentWindowInsets = ZeroInsets,

        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CampoBusca(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Buscar por nome da avaliação",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                    avaliacoesComIdValido.isEmpty() && !isLoading && errorMessage == null && searchQuery.isBlank() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) { Text("Nenhuma avaliação cadastrada.", style = MaterialTheme.typography.bodyLarge) }
                    }
                    avaliacoesFiltrados.isEmpty() && searchQuery.isNotBlank() && !isLoading && errorMessage == null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nenhuma avaliação encontrada para \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    else -> {
                        if (isLoading && avaliacoesComIdValido.isNotEmpty()) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                            )
                        }

                        val gruposPorDisciplina = remember(avaliacoesFiltrados) {
                            avaliacoesFiltrados.groupBy { it.disciplina?.id ?: -1L }
                                .toList()
                                .sortedBy { (_, lista) -> (lista.firstOrNull()?.disciplina?.nome ?: "\uFFFF").lowercase() }
                        }

                        val gruposAndamento = remember(gruposPorDisciplina) {
                            gruposPorDisciplina.filter { (_, lista) -> lista.any { it.estado != EstadoAvaliacao.CONCLUIDA } }
                        }
                        val gruposConcluidas = remember(gruposPorDisciplina) {
                            gruposPorDisciplina.filter { (_, lista) -> lista.isNotEmpty() && lista.all { it.estado == EstadoAvaliacao.CONCLUIDA } }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            item {
                                SecaoExpansivel(titulo = "Em andamento") {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        gruposAndamento.forEach { (discId, listaDaDisciplina) ->
                                            val nomeDisciplina =
                                                listaDaDisciplina.firstOrNull()?.disciplina?.nome
                                                    ?: "[Sem disciplina]"
                                            DisciplinaGrupoCard(
                                                nome = nomeDisciplina,
                                                podeAdicionar = discId != -1L,
                                                avaliacoes = listaDaDisciplina.sortedBy {
                                                    it.descricao ?: ""
                                                },
                                                onAddClick = { onAddAvaliacaoParaDisciplina(discId.toString()) },
                                                onAvaliacaoClick = { av ->
                                                    av.id?.let {
                                                        onNavigateToManterAvaliacao(
                                                            it.toString()
                                                        )
                                                    }
                                                },
                                                onExcluirClick = { av ->
                                                    av.id?.let {
                                                        avaliacaoParaExcluir = av
                                                        showConfirmDeleteDialog = true
                                                    }
                                                },
                                                onToggleConcluida = { av, marcado ->
                                                    if (marcado) avaliacaoParaConcluir =
                                                        av else avaliacaoParaReativar = av
                                                },
                                                onEditarNotaClick = { av ->
                                                    notaTemp = NotaCampo.fromDouble(av.nota)
                                                    notaDialogAvaliacao = av
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                SecaoExpansivel(titulo = "Concluídas", inicialExpandida = true) {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        gruposConcluidas.forEach { (discId, listaDaDisciplina) ->
                                            val nomeDisciplina =
                                                listaDaDisciplina.firstOrNull()?.disciplina?.nome
                                                    ?: "[Sem disciplina]"
                                            DisciplinaGrupoCard(
                                                nome = nomeDisciplina,
                                                podeAdicionar = discId != -1L,
                                                avaliacoes = listaDaDisciplina.sortedBy {
                                                    it.descricao ?: ""
                                                },
                                                onAddClick = { onAddAvaliacaoParaDisciplina(discId.toString()) },
                                                onAvaliacaoClick = { av ->
                                                    av.id?.let {
                                                        onNavigateToManterAvaliacao(
                                                            it.toString()
                                                        )
                                                    }
                                                },
                                                onExcluirClick = { av ->
                                                    av.id?.let {
                                                        avaliacaoParaExcluir = av
                                                        showConfirmDeleteDialog = true
                                                    }
                                                },
                                                onToggleConcluida = { av, marcado ->
                                                    if (marcado) avaliacaoParaConcluir =
                                                        av else avaliacaoParaReativar = av
                                                },
                                                onEditarNotaClick = { av ->
                                                    notaTemp = NotaCampo.fromDouble(av.nota)
                                                    notaDialogAvaliacao = av
                                                }
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
    )
}

@Composable
fun SecaoExpansivel(
    titulo: String,
    inicialExpandida: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(inicialExpandida) }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column { content() }
        }
    }
}

@Composable
fun DisciplinaGrupoCard(
    nome: String,
    podeAdicionar: Boolean,
    avaliacoes: List<Avaliacao>,
    onAddClick: () -> Unit,
    onAvaliacaoClick: (Avaliacao) -> Unit,
    onExcluirClick: (Avaliacao) -> Unit,
    onToggleConcluida: (Avaliacao, Boolean) -> Unit,
    onEditarNotaClick: (Avaliacao) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = LilasCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = nome,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {

                    if (avaliacoes.isEmpty()) {
                        Text(
                            "Sem avaliações nesta disciplina.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                        )
                    } else {
                        avaliacoes.forEach { av ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAvaliacaoClick(av) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = av.estado == EstadoAvaliacao.CONCLUIDA,
                                    onCheckedChange = { marcado -> onToggleConcluida(av, marcado) }
                                )

                                Column(Modifier.weight(1f)) {
                                    Text(av.descricao ?: "[sem descrição]")
                                    val dataFmt = formatarDataHora(av.dataEntrega)
                                    val subtitulo = buildString {
                                        av.tipoAvaliacao?.let { append(it) }
                                        if (dataFmt.isNotBlank()) {
                                            if (isNotEmpty()) append(" • ")
                                            append(dataFmt)
                                        }
                                    }
                                    if (subtitulo.isNotBlank()) {
                                        Text(
                                            text = subtitulo,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    TextButton(onClick = { onEditarNotaClick(av) }) {
                                        Text(
                                            if (av.nota != null) "Nota: ${NotaCampo.formatListValue(av.nota)}" else "Definir nota"
                                        )
                                    }
                                }

                                IconButton(onClick = { onExcluirClick(av) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                                }
                            }
                        }
                    }

                    if (podeAdicionar) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = onAddClick,
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = LilasButton,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) { Text("Adicionar avaliação") }
                        }
                    }
                }
            }
        }
    }
}