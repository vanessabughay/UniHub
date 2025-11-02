package com.example.unihub.ui.ListarDisciplinas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.unihub.notifications.AttendanceNotificationScheduler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.MaterialTheme.colorScheme

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
        viewModel.loadDisciplinas()
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }

    errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
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



    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddDisciplina,
                containerColor =  Color(0xFF5AB9D6),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(bottom = 35.dp)
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
                    item {
                        AnimatedVisibility(visible = ativasExpandidas) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                disciplinasAtivas.forEach { disciplina ->
                                    DisciplinaItem(
                                        disciplina = disciplina,
                                        onViewDisciplina = { onDisciplinaClick(disciplina.id.toString()) },
                                        onShareClicked = { d -> Toast.makeText(context, "Compartilhar ${d.nome}", Toast.LENGTH_SHORT).show() }
                                    )
                                }
                            }
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
                    item {
                        AnimatedVisibility(visible = inativasExpandidas) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                disciplinasInativas.forEach { disciplina ->
                                    DisciplinaItem(
                                        disciplina = disciplina,
                                        onViewDisciplina = { onDisciplinaClick(disciplina.id.toString()) },
                                        onShareClicked = { d -> Toast.makeText(context, "Compartilhar ${d.nome}", Toast.LENGTH_SHORT).show() }
                                    )
                                }
                            }
                        }
                    }
                }

                if (disciplinasAtivas.isEmpty() && disciplinasInativas.isEmpty()) {
                    item {
                        val msg = if (searchQuery.isBlank()) "Nenhuma disciplina cadastrada." else "Nenhuma disciplina encontrada para \"$searchQuery\""
                        Text(msg, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 16.dp))
                    }
                }
            }
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
                    text = "${disciplina.codigo} ${disciplina.nome}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (disciplina.horariosAulas.isNotEmpty()) {
                    disciplina.horariosAulas.forEach { horario ->
                        val inicio = String.format(
                            "%02d:%02d",
                            horario.horarioInicio / 60, horario.horarioInicio % 60
                        )
                        val fim = String.format(
                            "%02d:%02d",
                            horario.horarioFim / 60, horario.horarioFim % 60
                        )
                        Text(
                            text = "${horario.diaDaSemana} - Sala ${horario.sala} | Horário: $inicio - $fim",
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                } else {
                    Text(
                        text = "Sem horário definido",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color.Gray
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

            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { onShareClicked(disciplina) }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Compartilhar Disciplina",
                    tint = Color.Black
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