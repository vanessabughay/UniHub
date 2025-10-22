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
                horariosAulas = it.horariosAulas
            )
        }
        scheduler.scheduleNotifications(schedules)
    }

    LaunchedEffect(Unit) {
        viewModel.loadDisciplinas()
    }

    var searchQuery by remember { mutableStateOf("") }

    errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
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
                containerColor = Color(0xFFEFEFEF),
                contentColor = Color.Black
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
                        }
                    )
                }
            }
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
            .padding(vertical = 8.dp),
        onClick = onViewDisciplina,
        colors = CardDefaults.cardColors(
            containerColor = CardBackgroundColor
        ),
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
