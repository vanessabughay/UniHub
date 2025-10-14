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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.unihub.components.BotoesFormulario
import com.example.unihub.components.CampoCombobox
import com.example.unihub.components.CampoData
import com.example.unihub.components.CampoFormulario
import com.example.unihub.components.Header
import com.example.unihub.data.model.Status
import com.example.unihub.data.model.Priority
import com.example.unihub.data.model.Tarefa
import java.text.SimpleDateFormat
import java.util.*
import com.example.unihub.data.repository.TarefaRepository
import com.example.unihub.data.api.TarefaApi

private fun getDefaultPrazoForUI(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

@Composable
fun TarefaFormScreen(
    navController: NavHostController,
    quadroId: String,
    colunaId: String,
    tarefaId: String? = null,
    viewModelFactory: ViewModelProvider.Factory
) {

    val tarefaViewModel: TarefaFormViewModel = viewModel(factory = viewModelFactory)

    val context = LocalContext.current
    val isEditing = tarefaId != null
    val tarefaState by tarefaViewModel.tarefa.collectAsState()
    val isLoading by tarefaViewModel.isLoading.collectAsState()
    val formResult by tarefaViewModel.formResult.collectAsState()

    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var statusSelecionado by remember { mutableStateOf(Status.INICIADA) }
    var prazo by remember { mutableStateOf(getDefaultPrazoForUI()) }
    var ultimaAcao by remember { mutableStateOf<TarefaFormAction?>(null) }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    LaunchedEffect(key1 = tarefaId) {
        if (isEditing) {
            tarefaViewModel.carregarTarefa(quadroId, colunaId, tarefaId!!)
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

    LaunchedEffect(formResult) {
        when (val result = formResult) {
            TarefaFormResult.Success -> {
                val mensagem = when (ultimaAcao) {
                    TarefaFormAction.CREATE -> "Tarefa criada com sucesso!"
                    TarefaFormAction.UPDATE -> "Tarefa atualizada com sucesso!"
                    TarefaFormAction.DELETE -> "Tarefa excluída com sucesso!"
                    null -> "Operação realizada com sucesso!"
                }

                Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
                tarefaViewModel.resetFormResult()
                navController.popBackStack()
            }

            is TarefaFormResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                tarefaViewModel.resetFormResult()
            }
            else -> {}
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
                onVoltar = { navController.popBackStack() }
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

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }


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
                        ultimaAcao = TarefaFormAction.CREATE
                        tarefaViewModel.cadastrarTarefa(quadroId, colunaId, novaTarefa)
                    } else {
                        tarefaState?.let { tarefaCarregada ->
                            val tarefaAtualizada = tarefaCarregada.copy(
                                titulo = titulo,
                                descricao = if (descricao.isBlank()) null else descricao,
                                status = statusSelecionado,
                                prazo = prazo
                            )
                            ultimaAcao = TarefaFormAction.UPDATE
                            tarefaViewModel.atualizarTarefa(colunaId, tarefaAtualizada)
                        }
                    }
                },
                onDelete = if (isEditing) {
                    {
                        ultimaAcao = TarefaFormAction.DELETE
                        tarefaId?.let { id -> tarefaViewModel.excluirTarefa(colunaId, id) }
                    }
                } else null
            )
        }
    }
}

private enum class TarefaFormAction {
    CREATE,
    UPDATE,
    DELETE
}









class FakeTarefaRepository : TarefaRepository(object : TarefaApi {
    override suspend fun getTarefa(colunaId: String, tarefaId: String): Tarefa {
        // Retorna um objeto de tarefa mockado para a prévia
        return Tarefa(
            id = tarefaId,
            descricao = "Esta é uma descrição de exemplo.",
            status = Status.INICIADA,
            prazo = System.currentTimeMillis() + 86400000,
            dataInicio = System.currentTimeMillis()
        )
    }

    override suspend fun createTarefa(
        quadroId: String,
        colunaId: String,
        tarefa: com.example.unihub.data.dto.TarefaPlanejamentoRequestDto
    ) {
        // Nada a ser feito aqui para o mock
    }

    override suspend fun updateTarefa(colunaId: String, tarefaId: String, tarefa: Tarefa): Tarefa {
        return tarefa
    }

    override suspend fun deleteTarefa(colunaId: String, tarefaId: String) {
        // Nada a ser feito aqui
    }
}) {
    // Essa classe pode ficar vazia, já que a lógica de mock está na interface.
}

// Uma fábrica de ViewModel falsa para o preview, que injeta o repositório falso.
class FakeTarefaFormViewModelFactory : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TarefaFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TarefaFormViewModel(FakeTarefaRepository()) as T
        }
        throw IllegalArgumentException("Classe de ViewModel desconhecida")
    }
}

// As suas funções de pré-visualização.
@Preview(showBackground = true)
@Composable
fun TarefaFormScreenPreview() {
    TarefaFormScreen(
        navController = rememberNavController(),
        quadroId = "id-do-quadro-exemplo",
        colunaId = "id-da-coluna-exemplo",
        tarefaId = null,
        viewModelFactory = FakeTarefaFormViewModelFactory()
    )
}

@Preview(showBackground = true)
@Composable
fun TarefaFormScreenEditingPreview() {
    TarefaFormScreen(
        navController = rememberNavController(),
        quadroId = "id-do-quadro-exemplo",
        colunaId = "id-da-coluna-exemplo",
        tarefaId = "id-da-tarefa-exemplo",
        viewModelFactory = FakeTarefaFormViewModelFactory()
    )
}