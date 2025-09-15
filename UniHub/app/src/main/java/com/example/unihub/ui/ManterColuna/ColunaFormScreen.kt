package com.example.unihub.ui.ManterColuna

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
import com.example.unihub.data.model.Coluna
import com.example.unihub.data.repository.TarefaApi
import com.example.unihub.data.repository.TarefaRepository
import com.example.unihub.data.model.Tarefa
import com.example.unihub.data.repository.ColunaRepository
import java.text.SimpleDateFormat
import java.util.*
import com.example.unihub.data.repository.ColunaApi

@Composable
fun ColunaFormScreen(
    navController: NavHostController,
    colunaId: String? = null,
    viewModelFactory: ViewModelProvider.Factory
) {
    val viewModel: ColunaFormViewModel = viewModel(factory = viewModelFactory)

    val context = LocalContext.current
    val isEditing = colunaId != null
    val colunaState by viewModel.coluna.collectAsState()

    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var prioridade by remember { mutableStateOf(Priority.MEDIA) }
    var status by remember { mutableStateOf(Status.INICIADA) }
    var prazo by remember { mutableStateOf(System.currentTimeMillis()) }


    LaunchedEffect(key1 = colunaId) {
        if (isEditing) {
            viewModel.carregarColuna(colunaId!!)
        }
    }

    LaunchedEffect(key1 = colunaState) {
        if (isEditing) {
            colunaState?.let { loadedColuna ->
                titulo = loadedColuna.titulo
                descricao = loadedColuna.descricao ?: ""
                prioridade = loadedColuna.prioridade
                status = loadedColuna.status
                prazo = loadedColuna.prazoManual
            }
        }
    }

    val formResult by viewModel.formResult.collectAsState()
    LaunchedEffect(formResult) {
        when (val result = formResult) {
            is FormResult.Success -> {
                Toast.makeText(context, "Operação realizada com sucesso!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
                viewModel.resetFormResult()
            }
            is FormResult.Error -> {
                Toast.makeText(context, "Erro: ${result.message}", Toast.LENGTH_LONG).show()
                viewModel.resetFormResult()
            }
            else -> {}
        }
    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val showDatePicker = {
        val calendar = Calendar.getInstance().apply { timeInMillis = prazo }
        DatePickerDialog(context, { _, year, month, day ->
            calendar.set(year, month, day)
            prazo = calendar.timeInMillis
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 50.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header(
                titulo = if (isEditing) "Editar Coluna" else "Cadastrar Coluna",
                onVoltar = { navController.popBackStack() }
            )

            CampoFormulario(label = "Título", value = titulo, onValueChange = { titulo = it })
            CampoFormulario(label = "Descrição", value = descricao, onValueChange = { descricao = it })
            CampoCombobox(label = "Prioridade", options = Priority.values().toList(), selectedOption = prioridade, onOptionSelected = { prioridade = it }, optionToDisplayedString = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } })

            val canMarkAsCompleted = colunaState?.todasTarefasConcluidas ?: true
            val statusOptions = if (colunaState?.tarefas?.isNotEmpty() == true) {
                Status.values().toList().filter { it != Status.CONCLUIDA || canMarkAsCompleted || status == Status.CONCLUIDA }
            } else {
                Status.values().toList()
            }

            CampoCombobox(
                label = "Status",
                options = statusOptions,
                selectedOption = status,
                onOptionSelected = { newStatus ->
                    if (newStatus == Status.CONCLUIDA && colunaState?.tarefas?.isNotEmpty() == true && !canMarkAsCompleted) {
                        Toast.makeText(context, "Conclua todas as tarefas primeiro.", Toast.LENGTH_SHORT).show()
                    } else {
                        status = newStatus
                    }
                },
                optionToDisplayedString = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
            )

            CampoData(label = "Prazo", value = dateFormat.format(Date(prazo)), onClick = { showDatePicker() })

            Spacer(modifier = Modifier.weight(1f))

            BotoesFormulario(
                onConfirm = {
                    if (titulo.isNotBlank()) {
                        val colunaParaSalvar = Coluna(
                            id = if (isEditing) colunaId!! else "",
                            titulo = titulo,
                            descricao = descricao.ifBlank { null },
                            prioridade = prioridade,
                            status = status,
                            prazoManual = prazo,
                            tarefas = if (isEditing) colunaState?.tarefas ?: emptyList() else emptyList()
                        )
                        viewModel.salvarOuAtualizarColuna(colunaParaSalvar)
                    } else {
                        Toast.makeText(context, "O título é obrigatório.", Toast.LENGTH_SHORT).show()
                    }
                },
                onDelete = if (isEditing) { { viewModel.excluirColuna(colunaId!!) } } else null
            )
        }
    }
}



// Um repositório falso que simula a API real, mas sem chamadas de rede.
class FakeColunaRepository : ColunaRepository(object : ColunaApi {
    override suspend fun getColunas(): List<Coluna> {
        return emptyList()
    }

    override suspend fun getColunaById(colunaId: String): Coluna {
        // Retorna um objeto de coluna de exemplo para o modo de edição.
        return Coluna(
            id = colunaId,
            titulo = "Coluna de Exemplo",
            descricao = "Esta é uma descrição de exemplo para o preview.",
            prioridade = Priority.MEDIA,
            status = Status.INICIADA,
            prazoManual = System.currentTimeMillis() + 86400000L,
            dataInicio = System.currentTimeMillis(),
            tarefas = emptyList()
        )
    }

    override suspend fun addColuna(coluna: Coluna): Coluna {
        return coluna
    }

    override suspend fun updateColuna(colunaId: String, coluna: Coluna): Coluna {
        return coluna
    }

    override suspend fun deleteColuna(colunaId: String) {
        // Não faz nada, já que é uma simulação.
    }
})

// Uma fábrica de ViewModel falsa que injeta o repositório falso no ViewModel.
class FakeColunaFormViewModelFactory : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ColunaFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ColunaFormViewModel(FakeColunaRepository()) as T
        }
        throw IllegalArgumentException("Classe de ViewModel desconhecida")
    }
}












// Prévia para o modo de cadastro de coluna.
@Preview(showBackground = true)
@Composable
fun ColunaFormScreenPreview() {
    ColunaFormScreen(
        navController = rememberNavController(),
        colunaId = null, // null para o modo de cadastro
        viewModelFactory = FakeColunaFormViewModelFactory()
    )
}

// Prévia para o modo de edição de coluna.
@Preview(showBackground = true)
@Composable
fun ColunaFormScreenEditingPreview() {
    ColunaFormScreen(
        navController = rememberNavController(),
        colunaId = "id-da-coluna-exemplo", // Um ID de exemplo para o modo de edição
        viewModelFactory = FakeColunaFormViewModelFactory()
    )
}