package com.example.unihub.ui.ListarDisciplinas

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unihub.components.CabecalhoPrincipal
import com.example.unihub.components.SearchBox
// O import de 'Disciplina' não é mais usado aqui, mas foi mantido como pediu.
// A classe 'DisciplinaResumo' foi adicionada abaixo para garantir que o código funcione.
import com.example.unihub.data.model.Disciplina

data class DisciplinaResumo(
    val id: String,
    val nome: String,
    val dia: String,
    val sala: String,
    val horario: String
)

// Cores definidas
val CardBackgroundColor = Color(0xFFD9EDF6)
val CardBackgroundColorSelected = Color(0xFFB2DDF3)


@Composable
fun ListarDisciplinasScreen(
    onAddDisciplina: () -> Unit,
    onDisciplinaDoubleClick: (disciplinaId: String) -> Unit
) {
    val context = LocalContext.current

    // Lista de disciplinas exemplo
    val todasDisciplinas = remember {
        listOf(
            DisciplinaResumo("DS436", "Engenharia de Software I", "Terça-feira", "A13", "19:00 - 22:00"),
            DisciplinaResumo("DS437", "Banco de Dados II", "Quarta-feira", "C02", "19:00 - 22:00"),
            DisciplinaResumo("DS438", "Redes de Computadores", "Quinta-feira", "A09", "19:00 - 22:00"),
            DisciplinaResumo("DS439", "Análise de Algoritmos", "Sexta-feira", "A15", "19:00 - 22:00"),
            DisciplinaResumo("DS440", "Cálculo Numérico", "Segunda-feira", "B01", "08:00 - 11:00"),
            DisciplinaResumo("DS441", "Sistemas Operacionais", "Terça-feira", "D05", "14:00 - 17:00"),
            DisciplinaResumo("DS442", "Compiladores", "Quarta-feira", "A03", "10:00 - 13:00"),
            DisciplinaResumo("DS443", "Segurança da Informação", "Quinta-feira", "C10", "19:00 - 22:00"),
            DisciplinaResumo("DS444", "Inteligência Artificial", "Sexta-feira", "B07", "08:00 - 11:00"),
            DisciplinaResumo("DS445", "Projeto de Software", "Segunda-feira", "E02", "19:00 - 22:00")
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var idDisciplinaSelecionada by remember { mutableStateOf<String?>(null) }

    // filtro busca
    val disciplinasFiltradas = if (searchQuery.isBlank()) {
        todasDisciplinas
    } else {
        todasDisciplinas.filter {
            it.nome.contains(searchQuery, ignoreCase = true) ||
                    it.id.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddDisciplina,
                containerColor = CardBackgroundColorSelected,
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
                            Text(text = "Buscar por nome ou código", color = Color.Gray)
                        }
                    }
                }
            }

            items(disciplinasFiltradas) { disciplina ->
                DisciplinaItem(
                    disciplina = disciplina,
                    isSelected = (idDisciplinaSelecionada == disciplina.id),
                    onSingleClick = {
                        idDisciplinaSelecionada = if (idDisciplinaSelecionada == disciplina.id) {
                            null
                        } else {
                            disciplina.id
                        }
                    },
                    onDoubleClick = {
                        onDisciplinaDoubleClick(disciplina.id)
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
    disciplina: DisciplinaResumo,
    isSelected: Boolean,
    onSingleClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onShareClicked: (DisciplinaResumo) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .combinedClickable(
                onClick = { onSingleClick() },
                onDoubleClick = { onDoubleClick() }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                    // CORREÇÃO: Usando 'disciplina.id'
                    text = "${disciplina.id} ${disciplina.nome}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${disciplina.dia} - Sala ${disciplina.sala}\n${disciplina.horario}",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            if (isSelected) {
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
}


@Preview(showBackground = true)
@Composable
fun ListarDisciplinasScreenPreview() {
    MaterialTheme {
        ListarDisciplinasScreen(
            onAddDisciplina = {},
            onDisciplinaDoubleClick = {}
        )
    }
}