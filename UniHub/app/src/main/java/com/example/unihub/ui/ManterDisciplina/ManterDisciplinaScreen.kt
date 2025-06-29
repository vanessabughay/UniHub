package com.example.unihub.ui.ManterDisciplina

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.model.HorarioAula
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.components.CampoDisciplina
import java.util.*


val FormCardColor = Color(0x365AB9D6)
val ButtonConfirmColor = Color(0xFF5AB9D6)

data class AulaInfo(
    val id: Int = UUID.randomUUID().hashCode(),
    var dia: String = "Segunda-feira",
    var ensalamento: String = "",
    var horarioInicio: String = "",
    var horarioFim: String = ""
)

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
                    DropdownMenuItem(text = { Text(dia) }, onClick = { onDiaSelecionado(dia); expandido = false })
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

@androidx.annotation.RequiresExtension(extension = android.os.Build.VERSION_CODES.S, version = 7)
@Composable
fun ManterDisciplinaScreen(
    disciplinaId: String?,
    onVoltar: () -> Unit,
    viewModel: ManterDisciplinaViewModel = viewModel(factory = ManterDisciplinaViewModelFactory)
) {
    var codigo by remember { mutableStateOf("") }
    var periodo by remember { mutableStateOf("") }
    var nomeDisciplina by remember { mutableStateOf("") }
    var nomeProfessor by remember { mutableStateOf("") }
    var cargaHoraria by remember { mutableStateOf("") }
    var qtdAulasSemana by remember { mutableStateOf("1") }
    var dataInicioSemestre by remember { mutableStateOf("") }
    var dataFimSemestre by remember { mutableStateOf("") }
    var aulas by remember { mutableStateOf(listOf(AulaInfo())) }
    var emailProfessor by remember { mutableStateOf("") }
    var plataformas by remember { mutableStateOf("") }
    var telefoneProfessor by remember { mutableStateOf("") }
    var salaProfessor by remember { mutableStateOf("") }
    var isAtiva by remember { mutableStateOf(true) }

    LaunchedEffect(qtdAulasSemana) {
        val quantidade = qtdAulasSemana.toIntOrNull() ?: 0
        if (quantidade > aulas.size) aulas = aulas + List(quantidade - aulas.size) { AulaInfo() }
        else if (quantidade < aulas.size && quantidade >= 0) aulas = aulas.take(quantidade)
    }

    Scaffold(
        topBar = {
            CabecalhoAlternativo(
                titulo = if (disciplinaId == null) "Nova Disciplina" else "Editar Disciplina",
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
                        val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
                        val inicio = if (dataInicioSemestre.isNotBlank()) LocalDate.parse(dataInicioSemestre, formatter) else LocalDate.now()
                        val fim = if (dataFimSemestre.isNotBlank()) LocalDate.parse(dataFimSemestre, formatter) else inicio

                        val disciplina = Disciplina(
                            id = codigo,
                            nome = nomeDisciplina,
                            professor = nomeProfessor,
                            periodo = periodo,
                            cargaHoraria = cargaHoraria.toIntOrNull() ?: 0,
                            aulas = aulas.map { HorarioAula(it.dia, it.ensalamento, it.horarioInicio, it.horarioFim) },
                            dataInicioSemestre = inicio,
                            dataFimSemestre = fim,
                            emailProfessor = emailProfessor,
                            plataforma = plataformas,
                            telefoneProfessor = telefoneProfessor,
                            salaProfessor = salaProfessor,
                            isAtiva = isAtiva,
                            receberNotificacoes = true
                        )
                        viewModel.adicionarDisciplina(disciplina)
                        onVoltar()
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
                        CampoDeTextoComTitulo("ID Disciplina", codigo, { codigo = it }, Modifier.weight(1f), placeholder = "DSXXX")
                        CampoDeTextoComTitulo("Período", periodo, { periodo = it }, Modifier.weight(1f), placeholder = "20XX/X")
                    }
                    CampoDeTextoComTitulo("Nome da Disciplina", nomeDisciplina, { nomeDisciplina = it }, placeholder = "Disciplina X")
                    CampoDeTextoComTitulo("Nome do Professor", nomeProfessor, { nomeProfessor = it}, placeholder = "Professor X")
                }
            }

            item {
                CampoDisciplina(title = "Informações de Aula") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoDeTextoComTitulo("CH Total", cargaHoraria, { cargaHoraria = it }, Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        CampoDeTextoComTitulo("Aulas/Semana", qtdAulasSemana, { qtdAulasSemana = it.filter { c -> c.isDigit() } }, Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    aulas.forEachIndexed { index, aula ->
                        CampoSelecaoDia(
                            diaSelecionado = aula.dia,
                            onDiaSelecionado = { novoValor -> aulas = aulas.toMutableList().also { it[index] = aula.copy(dia = novoValor) } }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CampoDeTextoComTitulo("Ensalamento", aula.ensalamento, { novoValor -> aulas = aulas.toMutableList().also { it[index] = aula.copy(ensalamento = novoValor) } }, Modifier.weight(1f))
                            CampoDeTextoComTitulo("Início", aula.horarioInicio, { novoValor -> aulas = aulas.toMutableList().also { it[index] = aula.copy(horarioInicio = novoValor) } }, Modifier.weight(1f))
                            CampoDeTextoComTitulo("Fim", aula.horarioFim, { novoValor -> aulas = aulas.toMutableList().also { it[index] = aula.copy(horarioFim = novoValor) } }, Modifier.weight(1f))
                        }
                        if (index < aulas.size - 1) Divider(modifier = Modifier.padding(vertical = 12.dp))
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoDeData("Início do Semestre", dataInicioSemestre, { dataInicioSemestre = it }, Modifier.weight(1f))
                        CampoDeData("Fim do Semestre", dataFimSemestre, { dataFimSemestre = it }, Modifier.weight(1f))
                    }
                }
            }

            item {
                CampoDisciplina(title = "Informações do Professor") {
                    CampoDeTextoComTitulo("E-mail", emailProfessor, { emailProfessor = it }, placeholder = "professor@exemplo.com")
                    CampoDeTextoComTitulo("Plataformas utilizadas", plataformas, { plataformas = it })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                        CampoDeTextoComTitulo("Telefone", telefoneProfessor, { telefoneProfessor = it }, Modifier.weight(1f))
                        CampoDeTextoComTitulo("Sala do Professor", salaProfessor, { salaProfessor = it }, Modifier.weight(1f))
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
                    Switch(checked = isAtiva, onCheckedChange = { isAtiva = it }, colors = SwitchDefaults.colors(checkedTrackColor = ButtonConfirmColor))
                }
            }

            item {
                OutlinedButton(onClick = { /* Lógica de exclusão */ }, modifier = Modifier.fillMaxWidth().padding(start= 80.dp, end = 80.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFE91E1E))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Excluir Disciplina", color= Color(0xFFE91E1E))
                }
            }
        }
    }
}

@androidx.annotation.RequiresExtension(extension = android.os.Build.VERSION_CODES.S, version = 7)
@Preview(showBackground = true, widthDp = 380)
@Composable
fun ManterDisciplinaScreenPreview() {
    MaterialTheme { ManterDisciplinaScreen(disciplinaId = null, onVoltar = {}) }
}