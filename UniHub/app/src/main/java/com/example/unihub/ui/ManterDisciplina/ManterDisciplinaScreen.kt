package com.example.unihub.ui.ManterDisciplina // O pacote correto agora

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // Para itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Para ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete // Para Icons.Default.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.* // Para Card, Button, Text, etc.
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Para viewModel()
import com.example.unihub.components.CabecalhoAlternativo // Seu CabecalhoAlternativo
import com.example.unihub.components.CampoDisciplina // Seu CampoDisciplina
import com.example.unihub.data.model.HorarioAula // Seu modelo HorarioAula
import com.example.unihub.data.remote.DisciplinaApiService // Para o Preview
import com.example.unihub.data.remote.RetrofitClient // Para o Preview
import com.example.unihub.data.repository.DisciplinaRepository // Para o Preview
import com.example.unihub.ui.VisualizarDisciplina.ManterDisciplinaViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.Locale
import java.util.UUID

// Cores definidas (se forem as suas)
val FormCardColor = Color(0x365AB9D6)
val ButtonConfirmColor = Color(0xFF5AB9D6)

// --- Seus Componentes Auxiliares (Integrados com os tipos do ViewModel) ---

@Composable
fun CampoDeTextoComTitulo(
    titulo: String,
    valor: String,
    onValorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false,
    singleLine: Boolean = true
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
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            readOnly = readOnly
        )
    }
}

@Composable
fun CampoDeData(
    label: String,
    dataSelecionada: LocalDate?, // Recebe LocalDate?
    onDataSelecionada: (LocalDate) -> Unit, // Retorna LocalDate
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formattedDate = dataSelecionada?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: ""

    val initialYear = dataSelecionada?.year ?: Calendar.getInstance().get(Calendar.YEAR)
    val initialMonth = (dataSelecionada?.monthValue ?: Calendar.getInstance().get(Calendar.MONTH) + 1) - 1 // Month é 0-based
    val initialDay = dataSelecionada?.dayOfMonth ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth -> onDataSelecionada(LocalDate.of(year, month + 1, dayOfMonth)) },
        initialYear, initialMonth, initialDay
    )

    Column(modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = formattedDate,
            onValueChange = {}, // Não permite digitação direta
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
    val dias = listOf("Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado", "Domingo")
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
    horaSelecionada: String, // Mantendo String para HH:mm
    onHoraSelecionada: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val initialHour = try {
        LocalTime.parse(horaSelecionada, DateTimeFormatter.ofPattern("HH:mm")).hour
    } catch (e: DateTimeParseException) {
        LocalTime.now().hour
    }
    val initialMinute = try {
        LocalTime.parse(horaSelecionada, DateTimeFormatter.ofPattern("HH:mm")).minute
    } catch (e: DateTimeParseException) {
        LocalTime.now().minute
    }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, h, m -> onHoraSelecionada(String.format(Locale.getDefault(), "%02d:%02d", h, m)) },
        initialHour, initialMinute, true // is24HourView = true
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
            onValueChange = {}, // Não permite digitação direta
            readOnly = true,
            placeholder = { Text("HH:mm") },
            trailingIcon = { Icon(Icons.Default.Schedule, "Abrir Relógio") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { timePickerDialog.show() }
        )
    }
}

// --- SEU ManterDisciplinaScreen Principal (INTEGRADO AO VIEWMODEL) ---

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManterDisciplinaScreen(
    disciplinaId: String?, // ID da disciplina para editar (null para nova)
    onBack: () -> Unit, // Callback para voltar à tela anterior
    onSaveSuccess: () -> Unit, // Callback para sucesso ao salvar
    onDeleteSuccess: () -> Unit, // Callback para sucesso ao deletar
    viewModel: ManterDisciplinaViewModel = viewModel() // Injeção do ViewModel
) {
    val context = LocalContext.current
    val formState by viewModel.disciplinaFormState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val deleteSuccess by viewModel.deleteSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val isEditing = disciplinaId != null

    // Efeito para carregar a disciplina quando a tela é iniciada ou o ID muda
    LaunchedEffect(key1 = disciplinaId) {
        viewModel.loadDisciplina(disciplinaId)
    }

    // Efeito para exibir mensagens de sucesso e navegar após salvar
    LaunchedEffect(saveSuccess) {
        if (saveSuccess == true) {
            Toast.makeText(context, errorMessage ?: "Disciplina salva com sucesso!", Toast.LENGTH_SHORT).show()
            viewModel.clearSaveStatus()
            onSaveSuccess() // Navega de volta
        } else if (saveSuccess == false) {
            Toast.makeText(context, errorMessage ?: "Falha ao salvar disciplina.", Toast.LENGTH_LONG).show()
            viewModel.clearSaveStatus()
        }
    }

    // Efeito para exibir mensagens de sucesso e navegar após deletar
    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess == true) {
            Toast.makeText(context, errorMessage ?: "Disciplina deletada com sucesso!", Toast.LENGTH_SHORT).show()
            viewModel.clearDeleteStatus()
            onDeleteSuccess() // Navega de volta
        } else if (deleteSuccess == false) {
            Toast.makeText(context, errorMessage ?: "Falha ao deletar disciplina.", Toast.LENGTH_LONG).show()
            viewModel.clearDeleteStatus()
        }
    }

    // Efeito para exibir mensagens de erro gerais (não de sucesso/falha específica de salvar/deletar)
    LaunchedEffect(errorMessage) {
        if (errorMessage != null && saveSuccess == null && deleteSuccess == null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            // AQUI USAMOS SEU COMPONENTE CabecalhoAlternativo
            CabecalhoAlternativo(
                titulo = if (isEditing) "Editar Disciplina" else "Nova Disciplina",
                onVoltar = onBack,
                onIconeDireitaClick = if (isEditing) {
                    {
                        // Ação para o ícone da direita (neste caso, deletar)
                        if (!isLoading) {
                            // Idealmente, você mostraria um AlertDialog de confirmação aqui
                            viewModel.deleteDisciplina(disciplinaId!!)
                        }
                    }
                } else null // Não exibe o ícone se não estiver editando
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (!isLoading) {
                        viewModel.saveDisciplina()
                    }
                },
                modifier = Modifier.padding(16.dp),
                containerColor = ButtonConfirmColor,
                contentColor = Color.White
            ) {
                Text(text = if (isEditing) "Atualizar" else "Salvar")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Add, contentDescription = "Salvar Disciplina")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        content = { paddingValues ->
            // Indicador de carregamento em tela cheia
            if (isLoading && saveSuccess == null && deleteSuccess == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { // Informações Gerais
                        // ID da disciplina (somente leitura, se estiver editando)
                        if (isEditing) {
                            CampoDeTextoComTitulo(
                                titulo = "ID da Disciplina",
                                valor = formState.id ?: "",
                                onValorChange = { /* Não alterável */ },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        CampoDisciplina(title = "Informações Gerais") {
                            // Removi 'codigo' e 'periodo' como variáveis separadas
                            CampoDeTextoComTitulo(
                                titulo = "Nome da Disciplina",
                                valor = formState.nome,
                                onValorChange = viewModel::onNomeChanged,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = "Disciplina X" // Use o placeholder do seu código original
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CampoDeTextoComTitulo(
                                titulo = "Nome do Professor",
                                valor = formState.professor,
                                onValorChange = viewModel::onProfessorChanged,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = "Professor X" // Use o placeholder do seu código original
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CampoDeTextoComTitulo(
                                titulo = "Período (Ex: 2025.1)",
                                valor = formState.periodo,
                                onValorChange = viewModel::onPeriodoChanged,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = "20XX/X" // Use o placeholder do seu código original
                            )
                        }
                    }

                    item { // Parte 1 das Informações de Aula
                        CampoDisciplina(title = "Informações de Aula") {
                            CampoDeTextoComTitulo(
                                titulo = "Carga Horária Total",
                                valor = formState.cargaHoraria,
                                onValorChange = viewModel::onCargaHorariaChanged,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                placeholder = "Ex: 60"
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Horários das Aulas (${formState.aulas.size} aulas)",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            if (formState.aulas.isEmpty()) {
                                Text("Nenhum horário de aula adicionado. Clique no botão abaixo para adicionar.", color = Color.Gray, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                    // Loop para os Horários de Aula individuais (FORA do 'item {}')
                    itemsIndexed(formState.aulas) { index, aula ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = FormCardColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Aula ${index + 1}", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                    IconButton(onClick = { viewModel.removeHorarioAula(aula) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remover Aula", tint = Color.Red)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                CampoSelecaoDia(
                                    diaSelecionado = aula.diaDaSemana,
                                    onDiaSelecionado = { novoDia ->
                                        viewModel.updateHorarioAula(aula, aula.copy(diaDaSemana = novoDia))
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                CampoDeTextoComTitulo(
                                    titulo = "Sala",
                                    valor = aula.sala,
                                    onValorChange = { novaSala ->
                                        viewModel.updateHorarioAula(aula, aula.copy(sala = novaSala))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = "Ex: B101"
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CampoDeHora(
                                        label = "Início",
                                        horaSelecionada = aula.horarioInicio,
                                        onHoraSelecionada = { novaHora ->
                                            viewModel.updateHorarioAula(aula, aula.copy(horarioInicio = novaHora))
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    CampoDeHora(
                                        label = "Fim",
                                        horaSelecionada = aula.horarioFim,
                                        onHoraSelecionada = { novaHora ->
                                            viewModel.updateHorarioAula(aula, aula.copy(horarioFim = novaHora))
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    item { // Parte 2 das Informações de Aula (botão e datas)
                        // Botão para adicionar novo horário
                        Button(
                            onClick = {
                                viewModel.addHorarioAula(HorarioAula(diaDaSemana = "Segunda-feira", sala = "", horarioInicio = "", horarioFim = ""))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FormCardColor)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar Horário")
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Adicionar Horário de Aula", color = MaterialTheme.colorScheme.onSurface)
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CampoDeData(
                                label = "Início do Semestre",
                                dataSelecionada = formState.dataInicioSemestre,
                                onDataSelecionada = viewModel::onDataInicioSemestreChanged,
                                modifier = Modifier.weight(1f)
                            )
                            CampoDeData(
                                label = "Fim do Semestre",
                                dataSelecionada = formState.dataFimSemestre,
                                onDataSelecionada = viewModel::onDataFimSemestreChanged,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item { // Informações do Professor
                        CampoDisciplina(title = "Informações do Professor") {
                            // Removi as variáveis locais e usei formState
                            CampoDeTextoComTitulo(
                                titulo = "E-mail",
                                valor = formState.emailProfessor,
                                onValorChange = viewModel::onEmailProfessorChanged,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = "professor@exemplo.com",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CampoDeTextoComTitulo(
                                titulo = "Plataformas utilizadas",
                                valor = formState.plataforma,
                                onValorChange = viewModel::onPlataformaChanged,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = "Ex: Google Classroom, Teams"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                                CampoDeTextoComTitulo(
                                    titulo = "Telefone",
                                    valor = formState.telefoneProfessor,
                                    onValorChange = viewModel::onTelefoneProfessorChanged,
                                    modifier = Modifier.weight(1f),
                                    placeholder = "(XX) XXXX-XXXX",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                )
                                CampoDeTextoComTitulo(
                                    titulo = "Sala do Professor",
                                    valor = formState.salaProfessor,
                                    onValorChange = viewModel::onSalaProfessorChanged,
                                    modifier = Modifier.weight(1f),
                                    placeholder = "Ex: B205"
                                )
                            }
                        }
                    }

                    item { // Switches de Ativação/Notificação
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Disciplina Ativa", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = formState.isAtiva,
                                onCheckedChange = viewModel::onIsAtivaChanged,
                                colors = SwitchDefaults.colors(checkedTrackColor = ButtonConfirmColor)
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Receber Notificações", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = formState.receberNotificacoes,
                                onCheckedChange = viewModel::onReceberNotificacoesChanged,
                                colors = SwitchDefaults.colors(checkedTrackColor = ButtonConfirmColor)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Botão de Excluir (somente visível se estiver editando)
                    if (isEditing) {
                        item {
                            OutlinedButton(
                                onClick = {
                                    if (!isLoading) {
                                        viewModel.deleteDisciplina(disciplinaId!!)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 60.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE91E1E)),
                                border = BorderStroke(1.dp, Color(0xFFE91E1E)), // CORRIGIDO AQUI
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir")
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text("Excluir Disciplina")
                            }
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    } else {
                        item {
                            Spacer(modifier = Modifier.height(80.dp)) // Espaço para o FAB em modo de criação
                        }
                    }
                }
            }
        }
    )
}

// --- Preview da Tela ---
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Preview(showBackground = true, widthDp = 380, heightDp = 1200)
@Composable
fun ManterDisciplinaScreenPreview() {
    MaterialTheme {
        val mockRepository = DisciplinaRepository(object : DisciplinaApiService {
            override suspend fun getDisciplinasResumoApi(): retrofit2.Response<List<com.example.unihub.data.remote.DisciplinaResumo>> =
                retrofit2.Response.success(emptyList())

            override suspend fun getDisciplinaByIdApi(id: String): retrofit2.Response<com.example.unihub.data.model.Disciplina> {
                return if (id == "DS001") {
                    retrofit2.Response.success(
                        com.example.unihub.data.model.Disciplina(
                            id = "DS001",
                            nome = "Projeto de Software Avançado",
                            professor = "Dra. Elara Viana",
                            periodo = "2025.1",
                            cargaHoraria = 120,
                            aulas = listOf(
                                HorarioAula("Segunda-feira", "B101", "08:00", "12:00"),
                                HorarioAula("Quarta-feira", "B101", "08:00", "12:00"),
                                HorarioAula("Sexta-feira", "Online", "14:00", "16:00")
                            ),
                            dataInicioSemestre = LocalDate.of(2025, 3, 1),
                            dataFimSemestre = LocalDate.of(2025, 7, 15),
                            emailProfessor = "elara.viana@exemplo.com",
                            plataforma = "Google Meet, Moodle",
                            telefoneProfessor = "(41) 99123-4567",
                            salaProfessor = "305-B",
                            isAtiva = true,
                            receberNotificacoes = true
                        )
                    )
                } else {
                    retrofit2.Response.success(null)
                }
            }

            override suspend fun addDisciplinaApi(disciplina: com.example.unihub.data.model.Disciplina): retrofit2.Response<com.example.unihub.data.model.Disciplina> =
                retrofit2.Response.success(disciplina.copy(id = "NEW_ID_" + UUID.randomUUID().toString().substring(0,4)))

            override suspend fun updateDisciplinaApi(id: String, disciplina: com.example.unihub.data.model.Disciplina): retrofit2.Response<com.example.unihub.data.model.Disciplina> =
                retrofit2.Response.success(disciplina)

            override suspend fun deleteDisciplinaApi(id: String): retrofit2.Response<Unit> =
                retrofit2.Response.success(Unit)
        })

        val disciplinaIdToPreview: String? = "DS001" // Altere para null para testar "Nova Disciplina"

        val mockViewModel = ManterDisciplinaViewModel(mockRepository)

        ManterDisciplinaScreen(
            disciplinaId = disciplinaIdToPreview,
            onBack = {},
            onSaveSuccess = { /* Handle preview save success */ },
            onDeleteSuccess = { /* Handle preview delete success */ },
            viewModel = mockViewModel
        )
    }
}