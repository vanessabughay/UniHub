package com.example.unihub.ui.ListarDisciplinas

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.components.CabecalhoPrincipal
import com.example.unihub.components.SearchBox

val CardBackgroundColor = Color(0xFFD9EDF6)
val CardBackgroundColorSelected = Color(0xFFB2DDF3)

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun ListarDisciplinasScreen(
    viewModel: ListarDisciplinasViewModel = viewModel(factory = ListarDisciplinasViewModelFactory),
    onAddDisciplina: () -> Unit,
    onDisciplinaDoubleClick: (disciplinaId: Long) -> Unit
) {
    val context = LocalContext.current
    val disciplinasState by viewModel.disciplinas.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var idDisciplinaSelecionada by remember { mutableStateOf<String?>(null) }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            item { CabecalhoPrincipal(titulo = "Disciplinas") }

            item {
                SearchBox(modifier = Modifier.padding(vertical = 16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        if (searchQuery.isEmpty()) {
                            Text(text = "Buscar por nome, código ou id", color = Color.Gray)
                        }
                    }
                }
            }

            items(disciplinasFiltradas) { disciplina ->
                DisciplinaItem(
                    disciplina = disciplina,
                    isSelected = (idDisciplinaSelecionada == disciplina.disciplinaId),
                    onSingleClick = {
                        idDisciplinaSelecionada = if (idDisciplinaSelecionada == disciplina.disciplinaId) null else disciplina.disciplinaId
                    },
                    onDoubleClick = {
                        disciplina.disciplinaId?.toLongOrNull()?.let { disciplinaIdLong ->
                            onDisciplinaDoubleClick(disciplinaIdLong)
                        }
                    },
                    onShareClicked = {
                        Toast.makeText(context, "Compartilhar ${it.nome}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DisciplinaItem(
    disciplina: DisciplinaResumoUi,
    isSelected: Boolean,
    onSingleClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onShareClicked: (DisciplinaResumoUi) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .combinedClickable(
                onClick = onSingleClick,
                onDoubleClick = onDoubleClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CardBackgroundColorSelected else CardBackgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${disciplina.codigo} - ${disciplina.nome}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (disciplina.horariosAulas.isNotEmpty()) {
                    disciplina.horariosAulas.forEach { horario ->
                        Text(
                            text = "${horario.diaDaSemana} - Sala ${horario.sala} | Horário: ${horario.horarioInicio} - ${horario.horarioFim}",
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                } else {
                    Text(
                        text = "Sem horários definidos",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color.Gray
                    )
                }
            }

            if (isSelected) {
                Spacer(modifier = Modifier.size(8.dp))
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
}
