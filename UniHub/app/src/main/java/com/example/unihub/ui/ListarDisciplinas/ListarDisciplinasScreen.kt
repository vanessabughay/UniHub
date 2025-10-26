package com.example.unihub.ui.ListarDisciplinas

import android.Manifest
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
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.components.CampoBusca
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.model.UsuarioResumo
import com.example.unihub.notifications.AttendanceNotificationScheduler

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

    val scheduler = remember { AttendanceNotificationScheduler(context.applicationContext) }

    val compartilhamentoViewModel: CompartilhamentoViewModel =
        viewModel(factory = CompartilhamentoViewModelFactory)

    val contatos by compartilhamentoViewModel.contatos.collectAsState()
    val notificacoes by compartilhamentoViewModel.notificacoes.collectAsState()
    val compartilhamentoErro by compartilhamentoViewModel.erro.collectAsState()
    val compartilhamentoStatus by compartilhamentoViewModel.statusMessage.collectAsState()
    val isCarregandoContatos by compartilhamentoViewModel.isCarregandoContatos.collectAsState()
    val convitesEmProcessamento by compartilhamentoViewModel.convitesEmProcessamento.collectAsState()
    val isCompartilhando by compartilhamentoViewModel.isCompartilhando.collectAsState()

    var disciplinaParaCompartilhar by remember { mutableStateOf<DisciplinaResumoUi?>(null) }

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
            AttendanceNotificationScheduler.DisciplineScheduleInfo(
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

    LaunchedEffect(Unit) {
        TokenManager.loadToken(context.applicationContext)
        TokenManager.usuarioId?.let { compartilhamentoViewModel.carregarNotificacoes(it) }
        viewModel.loadDisciplinas()
    }

    var searchQuery by remember { mutableStateOf("") }
    val usuarioId = TokenManager.usuarioId

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

    val disciplinasFiltradas = if (searchQuery.isBlank()) {
        disciplinasState
    } else {
        disciplinasState.filter {
            it.nome.contains(searchQuery, ignoreCase = true) ||
                    it.codigo.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddDisciplina,
                containerColor =  Color(0xFF5AB9D6),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Disciplina")
            }
        }
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
                placeholder = "Buscar por nome ou id",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            if (notificacoes.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Convites Recebidos",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    notificacoes.forEach { notificacao ->
                        val conviteId = notificacao.conviteId
                        NotificacaoConviteCard(
                            notificacao = notificacao,
                            onAceitar = conviteId?.let {
                                {
                                    if (usuarioId != null) {
                                        compartilhamentoViewModel.aceitarConvite(usuarioId, it)
                                    } else {
                                        Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onRejeitar = conviteId?.let {
                                {
                                    if (usuarioId != null) {
                                        compartilhamentoViewModel.rejeitarConvite(usuarioId, it)
                                    } else {
                                        Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            isProcessando = conviteId?.let { convitesEmProcessamento.contains(it) } == true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(disciplinasFiltradas) { disciplina ->
                    DisciplinaItem(
                        disciplina = disciplina,
                        onViewDisciplina = {
                            onDisciplinaClick(disciplina.id.toString())
                        },
                        onShareClicked = { d ->
                            Toast.makeText(
                                context,
                                "Compartilhar ${d.nome}",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (usuarioId != null) {
                                disciplinaParaCompartilhar = d
                                compartilhamentoViewModel.carregarContatos(usuarioId)
                            } else {
                                Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }
        }
    }

    disciplinaParaCompartilhar?.let { disciplinaSelecionada ->
        if (usuarioId != null) {
            CompartilharDisciplinaDialog(
                disciplina = disciplinaSelecionada,
                contatos = contatos,
                isLoading = isCarregandoContatos,
                isSending = isCompartilhando,
                onDismiss = { disciplinaParaCompartilhar = null },
                onConfirm = { contato, mensagem ->
                    compartilhamentoViewModel.compartilharDisciplina(
                        usuarioId,
                        disciplinaSelecionada.id,
                        contato.id,
                        mensagem
                    )
                    disciplinaParaCompartilhar = null
                }
            )
        } else {
            disciplinaParaCompartilhar = null
        }
    }
}

// -------- Item da lista --------

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
fun NotificacaoConviteCard(
    notificacao: NotificacaoConviteUi,
    onAceitar: (() -> Unit)?,
    onRejeitar: (() -> Unit)?,
    isProcessando: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = notificacao.mensagem,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            notificacao.criadaEm?.let { data ->
                Text(
                    text = data,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (onAceitar != null || onRejeitar != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isProcessando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        if (onRejeitar != null) {
                            TextButton(onClick = onRejeitar) {
                                Text("Rejeitar")
                            }
                        }
                        if (onAceitar != null) {
                            TextButton(onClick = onAceitar) {
                                Text("Aceitar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompartilharDisciplinaDialog(
    disciplina: DisciplinaResumoUi,
    contatos: List<UsuarioResumo>,
    isLoading: Boolean,
    isSending: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (UsuarioResumo, String?) -> Unit
) {
    var mensagem by remember { mutableStateOf("") }
    var contatoSelecionadoId by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    contatos.firstOrNull { it.id == contatoSelecionadoId }?.let { selecionado ->
                        onConfirm(selecionado, mensagem.takeIf { it.isNotBlank() })
                    }
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
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = mensagem,
                            onValueChange = { mensagem = it },
                            label = { Text("Mensagem (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false
                        )
                    }
                }
            }
        }
    )
}

private fun formatHorario(horario: Int): String {
    val horas = horario / 100
    val minutos = horario % 100
    return String.format("%02d:%02d", horas, minutos)
}