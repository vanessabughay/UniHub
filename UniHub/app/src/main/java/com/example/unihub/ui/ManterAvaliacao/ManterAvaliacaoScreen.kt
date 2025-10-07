// MANTERAVALIACAOSCREEN

package com.example.unihub.ui.ManterAvaliacao

import android.app.DatePickerDialog // Para DatePicker (se for ativar)
import android.app.TimePickerDialog   // Para TimePicker (se for ativar)
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.animation.core.copy
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange // Icon for Date
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccessTime // Icon for Time
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.data.model.Modalidade
import com.example.unihub.ui.ListarAvaliacao.CardDefaultBackgroundColor
import com.example.unihub.ui.ListarContato.ContatoResumoUi
import com.example.unihub.ui.ManterContato.DeleteButtonErrorColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class) // Para ExposedDropdownMenuBox
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun ManterAvaliacaoScreen(
    avaliacaoId: String?,
    disciplinaId: String?,
    viewModel: ManterAvaliacaoViewModel = viewModel(factory = ManterAvaliacaoViewModelFactory()),
    onVoltar: () -> Unit,
    onExcluirSucessoNavegarParaLista: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    var showAddIntegrantesDialog by remember { mutableStateOf(false) }
    var showRemoveIntegrantesDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current



    LaunchedEffect(uiState.sucesso, uiState.isExclusao) {
        if (uiState.sucesso) {
            if (uiState.isExclusao) {
                Toast.makeText(context, "Avaliação excluída com sucesso!", Toast.LENGTH_SHORT).show()
                onExcluirSucessoNavegarParaLista()
            } else {
                Toast.makeText(context, "Avaliação salva com sucesso!", Toast.LENGTH_SHORT).show()
                onVoltar()
            }
            viewModel.onEventoConsumido()
        }
    }

    LaunchedEffect(uiState.erro) {
        uiState.erro?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.onEventoConsumido()
        }
    }

    LaunchedEffect(avaliacaoId) {
        if (avaliacaoId != null) viewModel.loadAvaliacao(avaliacaoId)
    }

    LaunchedEffect(disciplinaId, avaliacaoId) {
        if (avaliacaoId == null && !disciplinaId.isNullOrBlank()) {
            viewModel.preselectDisciplinaId(disciplinaId)
        }
    }

    Scaffold(
        topBar = {
            CabecalhoAlternativo(
                titulo = if (avaliacaoId == null) "Nova Avaliação" else "Editar Avaliação",
                onVoltar = onVoltar
            )
        }
    ) { paddingValues ->

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmar Exclusão") },
                text = { Text("Tem certeza de que deseja excluir esta Avaliação?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            avaliacaoId?.let { viewModel.deleteAvaliacao(it) }
                            showDeleteDialog = false
                        }
                    ) { Text("Excluir", color = DeleteButtonErrorColor) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                }
            )
        }

        if (showAddIntegrantesDialog) {
            SelecaoContatosDialog(
                titulo = "Adicionar Integrantes",
                contatosDisponiveis = uiState.todosOsContatosDisponiveis.filter { c ->
                    !uiState.integrantesDaAvaliacao.any { integrante -> integrante.id == c.id}
                },
                idsContatosJaSelecionados = uiState.integrantesDaAvaliacao.map { it.id }.toSet(),
                onDismissRequest = { showAddIntegrantesDialog = false },
                onConfirmarSelecao = { idsSelecionadosParaAdicionar ->
                    idsSelecionadosParaAdicionar.forEach { viewModel.addIntegrantePeloId(it) }
                    showAddIntegrantesDialog = false
                },
                isLoading = uiState.isLoadingAllContatos,
                loadingError = uiState.errorLoadingAllContatos
            )
        }

        if (showRemoveIntegrantesDialog) {
            SelecaoContatosDialog(
                titulo = "Remover Integrantes",
                contatosDisponiveis = uiState.integrantesDaAvaliacao,
                idsContatosJaSelecionados = emptySet(), // Não aplicável para remoção
                onDismissRequest = { showRemoveIntegrantesDialog = false },
                onConfirmarSelecao = { idsSelecionadosParaRemover ->
                    idsSelecionadosParaRemover.forEach { viewModel.removeIntegrantePeloId(it) }
                    showRemoveIntegrantesDialog = false
                },
                isForRemoval = true,
                isLoading = false,
                loadingError = null
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card para os DADOS DA AVALIAÇÃO
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CardDefaultBackgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Dados da Avaliação",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // DISCIPLINA (Dropdown)
                    var disciplinaExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = disciplinaExpanded,
                        onExpandedChange = { disciplinaExpanded = !disciplinaExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = uiState.nomeDisciplinaSelecionada.ifEmpty { "Selecione uma Disciplina" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Disciplina *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = disciplinaExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors()
                        )
                        ExposedDropdownMenu(
                            expanded = disciplinaExpanded,
                            onDismissRequest = { disciplinaExpanded = false }
                        ) {
                            if (uiState.isLoadingDisciplinas) {
                                DropdownMenuItem(text = { Text("Carregando...") }, onClick = {}, enabled = false)
                            } else if (uiState.errorLoadingDisciplinas != null) {
                                DropdownMenuItem(text = { Text("Erro ao carregar", color = MaterialTheme.colorScheme.error) }, onClick = {}, enabled = false)
                            } else if (uiState.todasDisciplinasDisponiveis.isEmpty()){
                                DropdownMenuItem(text = { Text("Nenhuma disciplina") }, onClick = {}, enabled = false)
                            } else {
                                uiState.todasDisciplinasDisponiveis.forEach { disciplina ->
                                    DropdownMenuItem(
                                        text = { Text(disciplina.nome) },
                                        onClick = {
                                            viewModel.onDisciplinaSelecionada(disciplina)
                                            disciplinaExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // DESCRIÇÃO
                    OutlinedTextField(
                        value = uiState.descricao,
                        onValueChange = { viewModel.setDescricao(it) },
                        label = { Text("Descrição *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors()
                    )

                    // TIPO
                    OutlinedTextField(
                        value = uiState.tipoAvaliacao,
                        onValueChange = { viewModel.setTipoAvaliacao(it) },
                        label = { Text("Tipo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors()
                    )

                    // MODALIDADE (Dropdown)
                    var modalidadeExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = modalidadeExpanded,
                        onExpandedChange = { modalidadeExpanded = !modalidadeExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            // Assumindo que Modalidade.name é INDIVIDUAL ou EM_GRUPO
                            value = uiState.modalidade.name.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Modalidade *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modalidadeExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors()
                        )
                        ExposedDropdownMenu(
                            expanded = modalidadeExpanded,
                            onDismissRequest = { modalidadeExpanded = false }
                        ) {
                            uiState.todasModalidades.forEach { modalidade ->
                                DropdownMenuItem(
                                    text = { Text(modalidade.name.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }) },
                                    onClick = {
                                        viewModel.setModalidade(modalidade)
                                        modalidadeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // DATA DA ENTREGA (somente seleção, sem digitar)
                    CampoData(
                        label = "Data de Entrega",
                        value = stringDateToMillis(uiState.dataEntrega),
                        onDateSelected = { millis ->
                            val iso = millisToIsoDate(millis) // "AAAA-MM-DD"
                            viewModel.setDataEntrega(iso)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // HORA DA ENTREGA (somente seleção, sem digitar)
                    CampoHorario(
                        label = "Hora de Entrega",
                        value = stringTimeToMinutes(uiState.horaEntrega),
                        onTimeSelected = { totalMinutes ->
                            viewModel.setHoraEntrega(minutesToHHmm(totalMinutes)) // "HH:MM"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // NOTA
                    OutlinedTextField(
                        value = uiState.nota,
                        onValueChange = { viewModel.setNota(it) },
                        label = { Text("Nota") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors()
                    )

                    // PESO (Desativado para interação complexa por enquanto)
                    OutlinedTextField(
                        value = uiState.peso,
                        onValueChange = { viewModel.setPeso(it) },
                        label = { Text("Peso") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = true, // Mude para false para desativar completamente a digitação
                        colors = OutlinedTextFieldDefaults.colors()
                    )

                    // PRIORIDADE (Dropdown)
                    var prioridadeExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = prioridadeExpanded,
                        onExpandedChange = { prioridadeExpanded = !prioridadeExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = uiState.prioridade.displayName, // Assumindo que Prioridade tem 'displayName'
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Prioridade *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = prioridadeExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors()
                        )
                        ExposedDropdownMenu(
                            expanded = prioridadeExpanded,
                            onDismissRequest = { prioridadeExpanded = false }
                        ) {
                            uiState.todasPrioridades.forEach { prioridade ->
                                DropdownMenuItem(
                                    text = { Text(prioridade.displayName) },
                                    onClick = {
                                        viewModel.setPrioridade(prioridade)
                                        prioridadeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // RECEBER NOTIFICAÇÕES (Checkbox)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setReceberNotificacoes(!uiState.receberNotificacoes) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.receberNotificacoes,
                            onCheckedChange = { viewModel.setReceberNotificacoes(it) }
                        )
                        Text(
                            text = "Receber Notificações",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // CARD PARA GERENCIAR INTEGRANTES (Apenas se modalidade for EM_GRUPO)
            if (uiState.modalidade == Modalidade.EM_GRUPO) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDefaultBackgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Integrantes da Avaliação",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (uiState.integrantesDaAvaliacao.isNotEmpty()) {
                            uiState.integrantesDaAvaliacao.take(5).forEach { integrante ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                    Icon(Icons.Filled.Person, contentDescription = "Integrante", modifier = Modifier.padding(end = 8.dp))
                                    Text(integrante.nome, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            if (uiState.integrantesDaAvaliacao.size > 5) {
                                Text("... e mais ${uiState.integrantesDaAvaliacao.size - 5}", style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            Text("Nenhum integrante adicionado.", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showAddIntegrantesDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Filled.AddCircle, contentDescription = "Adicionar", modifier = Modifier.padding(end = 4.dp))
                                Text("Adicionar")
                            }
                            Button(
                                onClick = { showRemoveIntegrantesDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                enabled = uiState.integrantesDaAvaliacao.isNotEmpty()
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remover", modifier = Modifier.padding(end = 4.dp))
                                Text("Remover")
                            }
                        }
                    }
                }
            }


            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))
            }

            // Botões de Ação
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button( // BOTÃO DE CONFIRMAR
                    onClick = {
                        if (avaliacaoId == null) {
                            viewModel.createAvaliacao()
                        } else {
                            viewModel.updateAvaliacao()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        if (avaliacaoId == null) "Confirmar" else "Salvar Alterações",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (avaliacaoId != null) {
                    Button( // BOTÃO DE EXCLUIR
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeleteButtonErrorColor.copy(alpha = 0.1f), // Ou MaterialTheme.colorScheme.errorContainer
                            contentColor = DeleteButtonErrorColor // Ou MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Excluir Avaliação", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Espaço extra no final
        }
    }
}

sealed class Screen(val route: String) {

    object ManterAvaliacao : Screen("manter_avaliacao") {
        fun createRoute(
            avaliacaoId: String? = null,
            disciplinaId: String? = null
        ): String {
            val qs = buildList {
                if (!avaliacaoId.isNullOrBlank()) add("id=$avaliacaoId")
                if (!disciplinaId.isNullOrBlank()) add("disciplinaId=$disciplinaId")
            }.joinToString("&")
            return if (qs.isEmpty()) route else "$route?$qs"
        }
    }
}


private fun stringDateToMillis(isoDate: String?): Long {
    if (isoDate.isNullOrBlank()) return 0L
    return try {
        val parts = isoDate.split("-").map { it.toInt() } // AAAA-MM-DD
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, parts[0])
            set(Calendar.MONTH, parts[1] - 1)
            set(Calendar.DAY_OF_MONTH, parts[2])
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        cal.timeInMillis
    } catch (_: Exception) { 0L }
}

private fun millisToIsoDate(millis: Long): String {
    if (millis <= 0L) return ""
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val y = cal.get(Calendar.YEAR)
    val m = cal.get(Calendar.MONTH) + 1
    val d = cal.get(Calendar.DAY_OF_MONTH)
    return String.format("%04d-%02d-%02d", y, m, d) // AAAA-MM-DD
}

private fun stringTimeToMinutes(hhmm: String?): Int {
    if (hhmm.isNullOrBlank()) return 0
    return try {
        val (h, m) = hhmm.split(":").map { it.toInt() }
        (h.coerceIn(0, 23) * 60) + m.coerceIn(0, 59)
    } catch (_: Exception) { 0 }
}

private fun minutesToHHmm(total: Int): String {
    val h = (total / 60).coerceIn(0, 23)
    val m = (total % 60).coerceIn(0, 59)
    return "%02d:%02d".format(h, m)
}



@Composable
fun SelecaoContatosDialog(
    titulo: String,
    contatosDisponiveis: List<ContatoResumoUi>,
    idsContatosJaSelecionados: Set<Long> = emptySet(), // Usado para desabilitar já selecionados na adição
    onDismissRequest: () -> Unit,
    onConfirmarSelecao: (Set<Long>) -> Unit,
    isLoading: Boolean = false,
    loadingError: String? = null,
    isForRemoval: Boolean = false
) {
    var idsTemporariamenteSelecionados by remember { mutableStateOf(emptySet<Long>()) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(titulo) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isLoading) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (loadingError != null) {
                    Text("Erro: $loadingError", color = MaterialTheme.colorScheme.error)
                } else if (contatosDisponiveis.isEmpty()) {
                    Text(if (isForRemoval) "Nenhum integrante para remover." else "Nenhum contato disponível.")
                } else {
                    Surface( // Adicionado Surface para melhor controle de altura e possível scroll interno
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp) // Limita altura
                    ) {
                        LazyColumn {
                            items(contatosDisponiveis, key = { it.id }) { contato ->
                                val isChecked = idsTemporariamenteSelecionados.contains(contato.id)
                                // Na adição, desabilitar se já for integrante (via idsContatosJaSelecionados)
                                // Na remoção, todos os listados (que são os integrantesDaAvaliacao) são habilitados para seleção
                                val isEnabled = isForRemoval || !idsContatosJaSelecionados.contains(contato.id)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = isEnabled) {
                                            if (isEnabled) {
                                                idsTemporariamenteSelecionados = if (isChecked) {
                                                    idsTemporariamenteSelecionados - contato.id
                                                } else {
                                                    idsTemporariamenteSelecionados + contato.id
                                                }
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = null, // Controlado pelo Row clickable
                                        enabled = isEnabled
                                    )
                                    Text(
                                        text = contato.nome + if (idsContatosJaSelecionados.contains(contato.id) && !isForRemoval) " (Já é integrante)" else "",
                                        modifier = Modifier.padding(start = 8.dp),
                                        color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmarSelecao(idsTemporariamenteSelecionados)
                },
                enabled = !isLoading && loadingError == null && (isForRemoval || idsTemporariamenteSelecionados.isNotEmpty()) && contatosDisponiveis.isNotEmpty()
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun CampoData(
    label: String,
    value: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val showDatePicker = {
        val calendar = Calendar.getInstance().apply {
            if (value != 0L) timeInMillis = value
        }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(year, month, day)
                onDateSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(modifier = Modifier.clickable { showDatePicker() }) {
            TextField(
                value = if (value != 0L) dateFormat.format(Date(value)) else "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    disabledIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
fun CampoHorario(
    label: String,
    value: Int,
    onTimeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hour = value / 60
    val minute = value % 60

    val showTimePicker = {
        TimePickerDialog(
            context,
            { _, hourOfDay, minuteOfHour ->
                onTimeSelected(hourOfDay * 60 + minuteOfHour)
            },
            if (value > 0) hour else 12,
            if (value > 0) minute else 0,
            true
        ).show()
    }

    val displayText = if (value <= 0) "" else String.format("%02d:%02d", hour, minute)

    Column(modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(modifier = Modifier.clickable { showTimePicker() }) {
            TextField(
                value = displayText,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
                singleLine = true
            )
        }
    }
}

