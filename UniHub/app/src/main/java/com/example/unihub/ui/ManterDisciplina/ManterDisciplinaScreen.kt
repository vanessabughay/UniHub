package com.example.unihub.ui.ManterDisciplina

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.components.CampoDisciplina
import com.example.unihub.data.model.HorarioAula
import kotlinx.coroutines.launch
import com.example.unihub.data.model.Disciplina
import androidx.lifecycle.viewmodel.compose.viewModel


val FormCardColor = Color(0x365AB9D6)
val ButtonConfirmColor = Color(0xFF5AB9D6)

@Composable
fun CampoDeTextoComTitulo(
    titulo: String,
    valor: String,
    onValorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = keyboardOptions
        )
    }
}

@Composable
fun CampoDeData(
    label: String,
    dataSelecionada: String,
    onDataSelecionada: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendario = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, ano, mes, dia -> onDataSelecionada("$dia/${mes + 1}/$ano") },
        calendario.get(Calendar.YEAR),
        calendario.get(Calendar.MONTH),
        calendario.get(Calendar.DAY_OF_MONTH)
    )

    Column(modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = dataSelecionada,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.CalendarToday, "Abrir Calendário") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoSelecaoDia(
    diaSelecionado: String,
    onDiaSelecionado: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dias = listOf("Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado")
    var expandido by remember { mutableStateOf(false) }

    Column(modifier) {
        Text(
            text = "Dia da Semana",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expandido,
            onExpandedChange = { expandido = !expandido }
        ) {
            OutlinedTextField(
                value = diaSelecionado,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                dias.forEach { dia ->
                    DropdownMenuItem(
                        text = { Text(dia) },
                        onClick = {
                            onDiaSelecionado(dia)
                            expandido = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CampoDeHora(
    label: String,
    horaSelecionada: String,
    onHoraSelecionada: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendario = Calendar.getInstance()
    val hora = calendario.get(Calendar.HOUR_OF_DAY)
    val minuto = calendario.get(Calendar.MINUTE)

    val timePickerDialog = TimePickerDialog(
        context,
        { _, h, m -> onHoraSelecionada(String.format("%02d:%02d", h, m)) },
        hora, minuto, true
    )

    Column(modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = horaSelecionada,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("HH:mm") },
            trailingIcon = { Icon(Icons.Default.Schedule, "Abrir Relógio") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { timePickerDialog.show() }
        )
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun ManterDisciplinaScreen(
    disciplinaId: Long?,
    onVoltar: () -> Unit
) {
    val factory = ManterDisciplinaViewModelFactory(disciplinaId)
    val viewModel: ManterDisciplinaViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val scope = rememberCoroutineScope()

    // Observa estados do ViewModel
    val codigo by remember { derivedStateOf { viewModel.codigo } }
    val periodo by remember { derivedStateOf { viewModel.periodo } }
    val nome by remember { derivedStateOf { viewModel.nome } }
    val professor by remember { derivedStateOf { viewModel.professor } }
    val cargaHoraria by remember { derivedStateOf { viewModel.cargaHoraria } }
    val aulas by remember { derivedStateOf { viewModel.aulas } }
    val dataInicioSemestre by remember { derivedStateOf { viewModel.dataInicioSemestre } }
    val dataFimSemestre by remember { derivedStateOf { viewModel.dataFimSemestre } }
    val emailProfessor by remember { derivedStateOf { viewModel.emailProfessor } }
    val plataforma by remember { derivedStateOf { viewModel.plataforma } }
    val telefoneProfessor by remember { derivedStateOf { viewModel.telefoneProfessor } }
    val salaProfessor by remember { derivedStateOf { viewModel.salaProfessor } }
    val isAtiva by remember { derivedStateOf { viewModel.isAtiva } }

    // Quantidade de aulas (pra controlar tamanho da lista)
    var qtdAulasSemana by remember { mutableStateOf(aulas.size.toString()) }
    LaunchedEffect(qtdAulasSemana) {
        val qtd = qtdAulasSemana.toIntOrNull() ?: 0
        viewModel.ajustarQuantidadeAulas(qtd)
    }

    Scaffold(
        topBar = {
            CabecalhoAlternativo(
                titulo = if (codigo.isEmpty()) "Nova Disciplina" else "Editar Disciplina",
                onVoltar = onVoltar
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.salvarDisciplina(
                                onSuccess = { onVoltar() },
                                onError = { /* Tratar erro, mostrar toast, etc */ }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonConfirmColor)
                ) {
                    Text("Confirmar")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                CampoDisciplina(title = "Informações Gerais") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoDeTextoComTitulo(
                            titulo = "Código da Disciplina",
                            valor = codigo,
                            onValorChange = { viewModel.codigo = it },
                            Modifier.weight(1f),
                            placeholder = "DSXXX"
                        )
                        CampoDeTextoComTitulo(
                            titulo = "Período",
                            valor = periodo,
                            onValorChange = { viewModel.periodo = it },
                            Modifier.weight(1f),
                            placeholder = "20XX/X"
                        )
                    }
                    CampoDeTextoComTitulo(
                        titulo = "Nome da Disciplina",
                        valor = nome,
                        onValorChange = { viewModel.nome = it },
                        placeholder = "Disciplina X"
                    )
                    CampoDeTextoComTitulo(
                        titulo = "Nome do Professor",
                        valor = professor,
                        onValorChange = { viewModel.professor = it },
                        placeholder = "Professor X"
                    )
                }
            }

            item {
                CampoDisciplina(title = "Informações de Aula") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoDeTextoComTitulo(
                            titulo = "CH Total",
                            valor = cargaHoraria,
                            onValorChange = { viewModel.cargaHoraria = it },
                            Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        CampoDeTextoComTitulo(
                            titulo = "Aulas/Semana",
                            valor = qtdAulasSemana,
                            onValorChange = {
                                qtdAulasSemana = it.filter { c -> c.isDigit() }
                            },
                            Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    aulas.forEachIndexed { index, aula ->
                        CampoSelecaoDia(
                            diaSelecionado = aula.diaDaSemana,
                            onDiaSelecionado = { novoDia ->
                                viewModel.atualizarAula(index, aula.copy(diaDaSemana = novoDia))
                            }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CampoDeTextoComTitulo(
                                titulo = "Ensalamento",
                                valor = aula.sala,
                                onValorChange = { novoValor ->
                                    viewModel.atualizarAula(index, aula.copy(sala = novoValor))
                                },
                                Modifier.weight(1f)
                            )
                            CampoDeHora(
                                label = "Início",
                                horaSelecionada = aula.horarioInicio,
                                onHoraSelecionada = { novaHora ->
                                    viewModel.atualizarAula(index, aula.copy(horarioInicio = novaHora))
                                },
                                Modifier.weight(1f)
                            )
                            CampoDeHora(
                                label = "Fim",
                                horaSelecionada = aula.horarioFim,
                                onHoraSelecionada = { novaHora ->
                                    viewModel.atualizarAula(index, aula.copy(horarioFim = novaHora))
                                },
                                Modifier.weight(1f)
                            )
                        }
                        if (index < aulas.size - 1) Divider(modifier = Modifier.padding(vertical = 12.dp))
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoDeData(
                            label = "Início do Semestre",
                            dataSelecionada = dataInicioSemestre,
                            onDataSelecionada = { viewModel.dataInicioSemestre = it },
                            Modifier.weight(1f)
                        )
                        CampoDeData(
                            label = "Fim do Semestre",
                            dataSelecionada = dataFimSemestre,
                            onDataSelecionada = { viewModel.dataFimSemestre = it },
                            Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                CampoDisciplina(title = "Informações do Professor") {
                    CampoDeTextoComTitulo(
                        titulo = "E-mail",
                        valor = emailProfessor,
                        onValorChange = { viewModel.emailProfessor = it },
                        placeholder = "professor@exemplo.com"
                    )
                    CampoDeTextoComTitulo(
                        titulo = "Plataformas utilizadas",
                        valor = plataforma,
                        onValorChange = { viewModel.plataforma = it }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoDeTextoComTitulo(
                            titulo = "Telefone",
                            valor = telefoneProfessor,
                            onValorChange = { viewModel.telefoneProfessor = it },
                            Modifier.weight(1f)
                        )
                        CampoDeTextoComTitulo(
                            titulo = "Sala do Professor",
                            valor = salaProfessor,
                            onValorChange = { viewModel.salaProfessor = it },
                            Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Disciplina Ativa", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = isAtiva,
                        onCheckedChange = { viewModel.isAtiva = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = ButtonConfirmColor)
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            try {
                                viewModel.excluirDisciplina(
                                    onSuccess = { onVoltar() },
                                    onError = { /* Tratar erro, mostrar toast, etc */ }
                                )
                            } catch (_: Exception) { }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(start = 80.dp, end = 80.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFE91E1E))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Excluir Disciplina", color = Color(0xFFE91E1E))
                }
            }
        }
    }
}
