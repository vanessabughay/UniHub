package com.example.unihub.ui.ManterAusencia

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import com.example.unihub.data.model.Categoria
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.data.model.Ausencia
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun ManterAusenciaScreen(
    disciplinaId: String,
    ausenciaId: String?,
    onVoltar: () -> Unit,
    viewModel: ManterAusenciaViewModel
) {
    val context = LocalContext.current

    var data by remember { mutableStateOf(LocalDate.now()) }
    var justificativa by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    val categorias by viewModel.categorias.collectAsState()
    var expandCategoria by remember { mutableStateOf(false) }
    var showAddCategoria by remember { mutableStateOf(false) }
    var novaCategoria by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val disciplina by viewModel.disciplina.collectAsState()
    val ausenciaLoaded by viewModel.ausencia.collectAsState()

    val showDatePicker = {
        val now = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                data = LocalDate.of(year, month + 1, dayOfMonth)
            },
            data.year,
            data.monthValue - 1,
            data.dayOfMonth
        ).show()
    }

    val sucesso by viewModel.sucesso.collectAsState()
    val erro by viewModel.erro.collectAsState()

    LaunchedEffect(disciplinaId) {
        viewModel.loadDisciplina(disciplinaId)
    }

    LaunchedEffect(ausenciaId) {
        ausenciaId?.let { viewModel.loadAusencia(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCategorias()
    }

    LaunchedEffect(sucesso) {
        if (sucesso) onVoltar()
    }

    LaunchedEffect(ausenciaLoaded) {
        ausenciaLoaded?.let { aus ->
            data = aus.data
            justificativa = aus.justificativa ?: ""
            categoria = aus.categoria ?: ""
        }
    }

    Scaffold(
        topBar = {
            CabecalhoAlternativo(
                titulo = if (ausenciaId == null) "Registrar Ausência" else "Editar Ausência",
                onVoltar = onVoltar
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .width(375.dp)
                .height(714.dp)
                .background(Color(0xFFF2F2F2))
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = disciplina?.nome ?: "",
                onValueChange = {},
                label = { Text("Disciplina") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false
            )
            OutlinedTextField(
                value = data.format(formatter),
                onValueChange = {},
                label = { Text("Data da ausência") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker() },
                readOnly = true
            )

            ExposedDropdownMenuBox(
                expanded = expandCategoria,
                onExpandedChange = { expandCategoria = !expandCategoria }
            ) {
                OutlinedTextField(
                    value = categoria,
                    onValueChange = {},
                    label = { Text("Categoria") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandCategoria) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandCategoria, onDismissRequest = { expandCategoria = false }) {
                    for (cat in categorias) {
                        DropdownMenuItem(
                            text = { Text(cat.nome) },
                            onClick = {
                                categoria = cat.nome
                                expandCategoria = false
                            }
                        )
                    }

                    HorizontalDivider()
                    DropdownMenuItem(text = { Text("Adicionar categoria") }, onClick = {
                        expandCategoria = false
                        showAddCategoria = true
                    })
                }
            }
            OutlinedTextField(
                value = justificativa,
                onValueChange = { justificativa = it },
                label = { Text("Justificativa") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val aus = Ausencia(
                        id = ausenciaId?.toLongOrNull(),
                        disciplinaId = disciplinaId.toLong(),
                        data = data,
                        categoria = categoria.takeIf { it.isNotBlank() },
                        justificativa = justificativa.takeIf { it.isNotBlank() }
                    )
                    if (ausenciaId == null) {
                        viewModel.criarAusencia(aus)
                    } else {
                        viewModel.atualizarAusencia(aus)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5AB9D6))
            ) { Text("Salvar", color = Color.Black) }
            erro?.let { Text(text = it, color = Color.Red) }

            if (ausenciaId != null) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Excluir Ausência", color = Color(0xFFE91E1E))
                }
            }

            if (showAddCategoria) {
                AlertDialog(
                    onDismissRequest = { showAddCategoria = false },
                    confirmButton = {
                        TextButton(onClick = {
                            if (novaCategoria.isNotBlank()) {
                                viewModel.addCategoria(novaCategoria) // Or viewModel.addCategoria(novaCategoria) depending on your VM
                                categoria = novaCategoria
                            }
                            novaCategoria = ""
                            showAddCategoria = false
                        }) { Text("Adicionar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddCategoria = false }) { Text("Cancelar") }
                    },
                    title = { Text("Nova Categoria") },
                    text = {
                        OutlinedTextField(
                            value = novaCategoria,
                            onValueChange = { novaCategoria = it },
                            label = { Text("Categoria") }
                        )
                    }
                )
            }
            if (showDeleteDialog && ausenciaId != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false
                            viewModel.deleteAusencia(ausenciaId.toLong())
                        }) { Text("Confirmar") }
                    },
                    dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } },
                    title = { Text("Confirmar exclusão") },
                    text = { Text("Tem certeza de que deseja excluir esta ausência?") }
                )
            }
        }
    }
}