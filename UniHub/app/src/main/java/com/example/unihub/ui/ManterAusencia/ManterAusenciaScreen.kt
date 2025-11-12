package com.example.unihub.ui.ManterAusencia

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.components.CampoData
import com.example.unihub.data.model.Ausencia
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.unihub.data.repository.AusenciaRepository
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.data.repository.CategoriaRepository
import com.example.unihub.data.apiBackend.ApiAusenciaBackend
import com.example.unihub.data.apiBackend.ApiDisciplinaBackend
import com.example.unihub.data.apiBackend.ApiCategoriaBackend
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.delay
import com.example.unihub.components.formatDateToLocale
import com.example.unihub.components.showLocalizedDatePicker
import com.example.unihub.ui.Shared.ZeroInsets


//cores
private val AusenciasCardColor = Color(0xFFF3E4F8)
private val AusenciasBtnColor = Color(0xFFE1C2F0)

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
    var isSaving by remember { mutableStateOf(false) }

    val locale = remember { Locale("pt", "BR") }
    val zoneId = remember { ZoneId.systemDefault() }

    val disciplina by viewModel.disciplina.collectAsState()
    val ausenciaLoaded by viewModel.ausencia.collectAsState()

    val showDatePicker = {
        val currentMillis = data.atStartOfDay(zoneId).toInstant().toEpochMilli()
        showLocalizedDatePicker(context, currentMillis, locale) { millis ->
            data = Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
        }
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
        viewModel.limparErro()
        viewModel.loadCategorias()
    }

    LaunchedEffect(erro) {
        if (erro != null) {
            delay(100)
            viewModel.limparErro()
        }
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
        },
        contentWindowInsets = ZeroInsets
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AusenciasCardColor),
                shape = RoundedCornerShape(16.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                val dataMillis = remember(data) {
                    data.atStartOfDay(zoneId).toInstant().toEpochMilli()
                }
                CampoData(
                    label = "Data da ausência",
                    value = formatDateToLocale(dataMillis, locale),
                    onClick = showDatePicker,
                    modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.fillMaxWidth(),
                )

                Button(
                    onClick = {
                        if (!isSaving) {
                            isSaving = true // desativa o botão e mostra "Salvando..."

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

                            // remember reseta ao recarregar a tela, reabilitando automaticamente
                        }
                    },
                    enabled = !isSaving, // evita múltiplos cliques
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AusenciasBtnColor)
                ) {
                    Text(
                        text = if (isSaving) "Salvando..." else "Salvar",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
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

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Preview(showBackground = true)
@Composable
fun ManterAusenciaScreenPreview() {
    ManterAusenciaScreen(
        disciplinaId = "1",
        ausenciaId = null,
        onVoltar = {},
        viewModel = ManterAusenciaViewModel(
            AusenciaRepository(ApiAusenciaBackend()),
            DisciplinaRepository(ApiDisciplinaBackend()),
            CategoriaRepository(ApiCategoriaBackend())
        )
    )
}
