package com.example.unihub.ui.ManterDisciplina

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Build
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
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.border
import androidx.compose.ui.viewinterop.AndroidView




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
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        OutlinedTextField(
            value = dataSelecionada,
            onValueChange = { onDataSelecionada(it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("DD/MM/AAAA") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        OutlinedTextField(
            value = horaSelecionada,
            onValueChange = { onHoraSelecionada(it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("HH:mm") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}


@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun ManterDisciplinaScreen(
    disciplinaId: String?,
    onVoltar: () -> Unit,
    viewModel: ManterDisciplinaViewModel
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
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(qtdAulasSemana) {
        val quantidade = qtdAulasSemana.toIntOrNull() ?: 0
        if (quantidade > aulas.size) aulas = aulas + List(quantidade - aulas.size) { AulaInfo() }
        else if (quantidade < aulas.size && quantidade >= 0) aulas = aulas.take(quantidade)
    }

    val context = LocalContext.current
    val disciplina = viewModel.disciplina.collectAsState()
    val erro = viewModel.erro.collectAsState()
    val sucesso = viewModel.sucesso.collectAsState()




    LaunchedEffect(disciplinaId) {
        if (disciplinaId != null) {
            viewModel.loadDisciplina(disciplinaId)
        }
    }


    LaunchedEffect(disciplina.value) {
        disciplina.value?.let { d ->
            codigo = d.codigo
            nomeDisciplina = d.nome
            nomeProfessor = d.professor
            periodo = d.periodo
            cargaHoraria = d.cargaHoraria.toString()
            qtdAulasSemana = d.aulas.size.toString()
            aulas = d.aulas.map {
                AulaInfo(
                    dia = it.diaDaSemana,
                    ensalamento = it.sala,
                    horarioInicio = it.horarioInicio,
                    horarioFim = it.horarioFim
                )
            }
            dataInicioSemestre = d.dataInicioSemestre.toString()
            dataFimSemestre = d.dataFimSemestre.toString()
            emailProfessor = d.emailProfessor
            plataformas = d.plataforma
            telefoneProfessor = d.telefoneProfessor
            salaProfessor = d.salaProfessor
            isAtiva = d.isAtiva
        }
    }


    LaunchedEffect(sucesso.value) {
        if (sucesso.value) {
            onVoltar()
        }
    }

    erro.value?.let {
        Toast.makeText(context, "Erro: $it", Toast.LENGTH_LONG).show()
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
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

                        val inicio = try { LocalDate.parse(dataInicioSemestre, formatter) } catch (e: Exception) { LocalDate.now() }
                        val fim = try { LocalDate.parse(dataFimSemestre, formatter) } catch (e: Exception) { LocalDate.now() }


                        val disciplina = com.example.unihub.data.model.Disciplina(
                            id = disciplinaId?.toLongOrNull(),
                            codigo = codigo,
                            nome = nomeDisciplina,
                            professor = nomeProfessor,
                            periodo = periodo,
                            cargaHoraria = cargaHoraria.toIntOrNull() ?: 0,
                            dataInicioSemestre = inicio, // ✅ já no formato certo
                            dataFimSemestre = fim,
                            emailProfessor = emailProfessor,
                            plataforma = plataformas,
                            telefoneProfessor = telefoneProfessor,
                            salaProfessor = salaProfessor,
                            isAtiva = isAtiva,
                            receberNotificacoes = true,
                            aulas = aulas.map {
                                com.example.unihub.data.model.HorarioAula(
                                    diaDaSemana = it.dia,
                                    sala = it.ensalamento,
                                    horarioInicio = it.horarioInicio,
                                    horarioFim = it.horarioFim
                                )
                            }
                        )

                        if (disciplinaId == null) {
                            viewModel.createDisciplina(disciplina)
                        } else {
                            viewModel.updateDisciplina(disciplina)
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
    )

    { paddingValues ->
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        disciplinaId?.let { viewModel.deleteDisciplina(it) }
                    }) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("Confirmar exclusão") },
                text = { Text("Tem certeza de que deseja excluir esta disciplina? Essa ação não pode ser desfeita.") }
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                CampoDisciplina(title = "Informações Gerais") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoDeTextoComTitulo("Código da Disciplina", codigo, { codigo = it }, Modifier.weight(1f), placeholder = "DSXXX")
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
                OutlinedButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 80.dp, end = 80.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFE91E1E))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Excluir Disciplina", color = Color(0xFFE91E1E))
                }

            }

        }
    }
}

//@Preview(showBackground = true, widthDp = 380)
//@Composable
//fun ManterDisciplinaScreenPreview() {
//   MaterialTheme { ManterDisciplinaScreen(disciplinaId = null, onVoltar = {}) }
//}