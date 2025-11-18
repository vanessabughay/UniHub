package com.example.unihub.ui.ListarDisciplinas

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.unihub.components.formatMinutesToTime
import com.example.unihub.ui.Shared.ZeroInsets

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.components.CampoBusca
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.model.UsuarioResumo
import com.example.unihub.notifications.FrequenciaNotificationScheduler
import com.example.unihub.notifications.CompartilhamentoNotificationActionReceiver
import com.example.unihub.notifications.CompartilhamentoNotificationSynchronizer


// Cores definidas
val CardBackgroundColor = Color(0xFFD9EDF6)

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun ListarDisciplinasScreen(
    viewModel: ListarDisciplinasViewModel = viewModel(factory = ListarDisciplinasViewModelFactory),
    onAddDisciplina: () -> Unit,
    onVoltar: () -> Unit,
    onDisciplinaClick: (disciplinaId: String) -> Unit
) {
    val context = LocalContext.current
    val disciplinasState by viewModel.disciplinas.collectAsState()

    val errorMessage by viewModel.errorMessage.collectAsState()

    val scheduler = remember { FrequenciaNotificationScheduler(context.applicationContext) }

    val compartilhamentoFactory = remember(context) {
        CompartilhamentoViewModelFactory(context.applicationContext)
    }
    val compartilhamentoViewModel: CompartilhamentoViewModel =
        viewModel(factory = compartilhamentoFactory)

    val contatos by compartilhamentoViewModel.contatos.collectAsState()
    val notificacoes by compartilhamentoViewModel.notificacoes.collectAsState()
    val compartilhamentoErro by compartilhamentoViewModel.erro.collectAsState()
    val compartilhamentoStatus by compartilhamentoViewModel.statusMessage.collectAsState()
    val isCarregandoContatos by compartilhamentoViewModel.isCarregandoContatos.collectAsState()
    val isCompartilhando by compartilhamentoViewModel.isCompartilhando.collectAsState()

    var disciplinaParaCompartilhar by remember { mutableStateOf<DisciplinaResumoUi?>(null) }
    val notificationSynchronizer = remember { CompartilhamentoNotificationSynchronizer.getInstance(context) }

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

    LaunchedEffect(disciplinasState) {
        val schedules = disciplinasState.map {
            FrequenciaNotificationScheduler.DisciplineScheduleInfo(
                id = it.id,
                nome = it.nome,
                receberNotificacoes = it.receberNotificacoes,
                horariosAulas = it.horariosAulas,
                totalAusencias = it.totalAusencias,
                ausenciasPermitidas = it.ausenciasPermitidas
            )
        }
        scheduler.scheduleNotifications(schedules)
    }
    var searchQuery by remember { mutableStateOf("") }
    var usuarioIdState by remember { mutableStateOf(TokenManager.usuarioId) }
    val latestUsuarioIdState = rememberUpdatedState(usuarioIdState)

    LaunchedEffect(Unit) {
        TokenManager.loadToken(context.applicationContext)
        val usuarioCarregado = TokenManager.usuarioId
        usuarioIdState = usuarioCarregado
        usuarioCarregado?.let { compartilhamentoViewModel.carregarNotificacoes(it) }
        viewModel.loadDisciplinas()
    }


    LaunchedEffect(notificacoes) {
        notificationSynchronizer.synchronize(notificacoes)
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val userId = latestUsuarioIdState.value
                if (userId != null) {
                    compartilhamentoViewModel.carregarNotificacoes(userId)
                }
            }
        }
        val filter = IntentFilter(CompartilhamentoNotificationActionReceiver.ACTION_REFRESH)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
    errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    compartilhamentoErro?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        compartilhamentoViewModel.consumirErro()
    }

    compartilhamentoStatus?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        if (message.contains("aceito", ignoreCase = true)) {
            viewModel.loadDisciplinas()
        }
        compartilhamentoViewModel.consumirStatus()
    }

    val disciplinasComBusca by remember(disciplinasState, searchQuery) {
        derivedStateOf {
            val filtradas = if (searchQuery.isBlank()) {
                disciplinasState
            } else {
                disciplinasState.filter {
                    it.nome.contains(searchQuery, ignoreCase = true) ||
                            it.codigo.contains(searchQuery, ignoreCase = true)
                }
            }
            filtradas.sortedBy { it.nome }
        }
    }


    val disciplinasAtivas by remember(disciplinasComBusca) {
        derivedStateOf { disciplinasComBusca.filter { it.isAtiva } }
    }

    val disciplinasInativas by remember(disciplinasComBusca) {
        derivedStateOf { disciplinasComBusca.filter { !it.isAtiva } }
    }

    var ativasExpandidas by rememberSaveable { mutableStateOf(true) }
    var inativasExpandidas by rememberSaveable { mutableStateOf(false) }

    val onShareDisciplina: (DisciplinaResumoUi) -> Unit = { disciplina ->
        Toast.makeText(
            context,
            "Compartilhar ${disciplina.nome}",
            Toast.LENGTH_SHORT
        ).show()
        val usuarioId = usuarioIdState
        if (usuarioId != null) {
            disciplinaParaCompartilhar = disciplina
            compartilhamentoViewModel.carregarContatos(usuarioId)
        } else {
            Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddDisciplina,
                containerColor =  Color(0xFF5AB9D6),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.navigationBarsPadding().padding(vertical = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Disciplina")
            }
        },
        contentWindowInsets = ZeroInsets
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabeçalho
            CabecalhoAlternativo(
                titulo = "Disciplinas",
                onVoltar = onVoltar,
            )

            // SearchBox
            CampoBusca(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Buscar por nome",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (disciplinasAtivas.isNotEmpty()) {
                    item {
                        TituloDeSecao(
                            titulo = "Disciplinas Ativas (${disciplinasAtivas.size})",
                            setaAbaixo = ativasExpandidas,
                            onClick = { ativasExpandidas = !ativasExpandidas }
                        )
                    }
                    if (ativasExpandidas) {
                        items(disciplinasAtivas, key = { it.id }) { disciplina ->
                            DisciplinaItem(
                                disciplina = disciplina,
                                onViewDisciplina = { onDisciplinaClick(disciplina.id.toString()) },
                                onShareClicked = onShareDisciplina
                            )
                        }
                    }
                }

                if (disciplinasInativas.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        TituloDeSecao(
                            titulo = "Disciplinas Inativas (${disciplinasInativas.size})",
                            setaAbaixo = inativasExpandidas,
                            onClick = { inativasExpandidas = !inativasExpandidas }
                        )
                    }
                    if (inativasExpandidas) {
                        items(disciplinasInativas, key = { it.id }) { disciplina ->
                            DisciplinaItem(
                                disciplina = disciplina,
                                onViewDisciplina = { onDisciplinaClick(disciplina.id.toString()) },
                                onShareClicked = onShareDisciplina
                            )
                        }
                    }
                }
                if (disciplinasAtivas.isEmpty() && disciplinasInativas.isEmpty()) {
                    item {
                        val msg = if (searchQuery.isBlank()) {
                            "Nenhuma disciplina cadastrada."
                        } else {
                            "Nenhuma disciplina encontrada para \"$searchQuery\""
                        }
                        Text(
                            msg,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }

    disciplinaParaCompartilhar?.let { disciplinaSelecionada ->
        val usuarioId = usuarioIdState
        if (usuarioId != null) {
            CompartilharDisciplinaDialog(
                disciplina = disciplinaSelecionada,
                contatos = contatos,
                isLoading = isCarregandoContatos,
                isSending = isCompartilhando,
                onDismiss = { disciplinaParaCompartilhar = null },
                onConfirm = { contato ->
                    compartilhamentoViewModel.compartilharDisciplina(
                        usuarioId,
                        disciplinaSelecionada.id,
                        contato.id
                    )
                    disciplinaParaCompartilhar = null
                }
            )
        } else {
            disciplinaParaCompartilhar = null
        }
    }
}




@Composable
fun DisciplinaItem(
    disciplina: DisciplinaResumoUi,
    onViewDisciplina: () -> Unit,
    onShareClicked: (DisciplinaResumoUi) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onViewDisciplina,
        colors = CardDefaults.cardColors(
            containerColor = CardBackgroundColor
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = disciplina.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Código: ${disciplina.codigo}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (disciplina.horariosAulas.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    disciplina.horariosAulas.take(2).forEach { horario ->
                        Text(
                            text = buildString {
                                append(horario.diaDaSemana)
                                append(" • ")
                                append(formatHorario(horario.horarioInicio))
                                append(" - ")
                                append(formatHorario(horario.horarioFim))
                                append(" • Sala ")
                                append(horario.sala)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (disciplina.horariosAulas.size > 2) {
                        Text(
                            text = "+${disciplina.horariosAulas.size - 2} horários cadastrados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Total de ausências: ${disciplina.totalAusencias}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                disciplina.ausenciasPermitidas?.let { limite ->
                    Text(
                        text = "Limite de ausências: $limite",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!disciplina.receberNotificacoes) {
                    Text(
                        text = "Notificações desativadas para esta disciplina",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            IconButton(onClick = { onShareClicked(disciplina) }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Compartilhar Disciplina",
                    tint = MaterialTheme.colorScheme.onSurface
                )
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
                .padding(vertical = 12.dp)
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
                fontSize = 20.sp,
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
fun CompartilharDisciplinaDialog(
    disciplina: DisciplinaResumoUi,
    contatos: List<UsuarioResumo>,
    isLoading: Boolean,
    isSending: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (UsuarioResumo) -> Unit
) {

    var contatoSelecionadoId by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    contatos.firstOrNull { it.id == contatoSelecionadoId }?.let(onConfirm)
                },
                enabled = contatoSelecionadoId != null && !isLoading && !isSending
            ) {
                Text("Compartilhar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text(text = "Compartilhar ${disciplina.nome}") },
        text = {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                contatos.isEmpty() -> {
                    Text("Você não possui contatos elegíveis para compartilhar esta disciplina.")
                }

                else -> {
                    Column {
                        LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                            items(contatos) { contato ->
                                val selecionado = contatoSelecionadoId == contato.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { contatoSelecionadoId = contato.id },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selecionado,
                                        onClick = { contatoSelecionadoId = contato.id }
                                    )
                                    Column(modifier = Modifier.padding(start = 8.dp)) {
                                        Text(
                                            text = contato.nome,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = contato.email,
                                            style = MaterialTheme.typography.bodySmall
                                        )
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

private fun formatHorario(horario: Int): String = formatMinutesToTime(horario)