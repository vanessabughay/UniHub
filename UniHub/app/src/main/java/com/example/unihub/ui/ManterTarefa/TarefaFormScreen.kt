package com.example.unihub.ui.ManterTarefa

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.unihub.components.BotoesFormulario
import com.example.unihub.components.CampoCombobox
import com.example.unihub.components.CampoData
import com.example.unihub.components.CampoFormulario
import com.example.unihub.components.Header
import com.example.unihub.data.model.Status
import com.example.unihub.data.model.Tarefa
import java.text.SimpleDateFormat
import java.util.*

private fun getDefaultPrazoForUI(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarefaFormScreen(
    navController: NavHostController,
    colunaId: String,
    tarefaId: String?, // Renomeado de subtarefaId para tarefaId
    TarefaViewModel: TarefaFormViewModel = viewModel()
) {
    val context = LocalContext.current
    val isEditing = tarefaId != null // Atualizada a verificação

    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var statusSelecionado by remember { mutableStateOf(Status.INICIADA) }
    var prazo by remember { mutableStateOf(getDefaultPrazoForUI()) }

    val tarefaState by TarefaViewModel.tarefa.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    LaunchedEffect(key1 = tarefaId) {
        if (isEditing) {
            // AQUI: a função no ViewModel também precisa ser renomeada para 'carregarTarefa'
            TarefaViewModel.carregarTarefa(colunaId, tarefaId!!)
        } else {
            titulo = ""
            descricao = ""
            statusSelecionado = Status.INICIADA
            prazo = getDefaultPrazoForUI()
        }
    }

    LaunchedEffect(key1 = tarefaState) {
        if (isEditing) {
            tarefaState?.let { loadedTarefa ->
                titulo = loadedTarefa.titulo
                descricao = loadedTarefa.descricao ?: ""
                statusSelecionado = loadedTarefa.status
                prazo = loadedTarefa.prazo
            }
        }
    }

    val showDatePicker = {
        val calendar = Calendar.getInstance().apply { timeInMillis = prazo }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                prazo = selectedCalendar.timeInMillis
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 50.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header(
                titulo = if (isEditing) "Editar tarefa" else "Cadastrar tarefa",
                onVoltar = { navController.popBackStack() },
                onClickIconeDireita = null
            )

            CampoFormulario(label = "Título", value = titulo, onValueChange = { titulo = it })
            CampoFormulario(label = "Descrição", value = descricao, onValueChange = { descricao = it })

            if (isEditing) {
                CampoCombobox(
                    label = "Status",
                    options = Status.values().toList(),
                    selectedOption = statusSelecionado,
                    onOptionSelected = { statusSelecionado = it },
                    optionToDisplayedString = { status ->
                        status.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }
                    },
                    placeholder = "Selecione o Status"
                )
            }

            CampoData(
                label = "Prazo",
                value = dateFormat.format(Date(prazo)),
                onClick = { showDatePicker() }
            )

            Spacer(modifier = Modifier.weight(1f))

            BotoesFormulario(
                onConfirm = {
                    if (titulo.isBlank()) {
                        Toast.makeText(context, "O título da tarefa é obrigatório.", Toast.LENGTH_SHORT).show()
                        return@BotoesFormulario
                    }

                    if (!isEditing) {
                        val novaTarefa = Tarefa(
                            titulo = titulo,
                            descricao = if (descricao.isBlank()) null else descricao,
                            status = Status.INICIADA, // Novas tarefas sempre iniciam com este status
                            prazo = prazo,
                            dataInicio = System.currentTimeMillis()
                        )
                        TarefaViewModel.cadastrarTarefa(colunaId, novaTarefa)
                    } else {
                        tarefaState?.let { tarefaCarregada ->
                            val tarefaAtualizada = tarefaCarregada.copy(
                                titulo = titulo,
                                descricao = if (descricao.isBlank()) null else descricao,
                                status = statusSelecionado,
                                prazo = prazo
                            )
                            TarefaViewModel.atualizarTarefa(colunaId, tarefaAtualizada)
                        }
                    }
                    navController.popBackStack()
                },
                onDelete = if (isEditing) {
                    {
                        // AQUI: Passa tarefaId, corrigido da lógica original
                        TarefaViewModel.excluirTarefa(colunaId, tarefaId!!)
                        navController.popBackStack()
                    }
                } else null
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TarefaFormScreenPreview() {
    // Para o modo de cadastro (não está editando)
    TarefaFormScreen(
        navController = rememberNavController(),
        colunaId = "id-da-coluna-exemplo",
        tarefaId = null // 'null' simula o modo de cadastro
    )
}

@Preview(showBackground = true)
@Composable
fun TarefaFormScreenEditingPreview() {
    // Para o modo de edição
    TarefaFormScreen(
        navController = rememberNavController(),
        colunaId = "id-da-coluna-exemplo",
        tarefaId = "id-da-tarefa-exemplo" // Um ID válido simula o modo de edição
    )
}