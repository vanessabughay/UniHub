package com.example.unihub.ui.ManterColuna

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
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.components.CampoCombobox
import com.example.unihub.components.CampoData
import com.example.unihub.components.CampoFormulario
import com.example.unihub.data.model.Status
import com.example.unihub.data.model.Coluna
import com.example.unihub.data.repository.ColunaRepository
import com.example.unihub.data.api.ColunaApi

@Composable
fun ColunaFormScreen(
    navController: NavHostController,
    quadroId: String,
    colunaId: String? = null,
    viewModelFactory: ViewModelProvider.Factory
) {
    val viewModel: ColunaFormViewModel = viewModel(factory = viewModelFactory)

    val context = LocalContext.current
    val isEditing = colunaId != null
    val colunaState by viewModel.coluna.collectAsState()

    var titulo by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(Status.INICIADA) }


    LaunchedEffect(key1 = colunaId) {
        if (isEditing) {
            viewModel.carregarColuna(quadroId, colunaId!!)
        }
    }

    LaunchedEffect(key1 = colunaState) {
        if (isEditing) {
            colunaState?.let { loadedColuna ->
                titulo = loadedColuna.titulo
                status = loadedColuna.status
            }
        }
    }

    val formResult by viewModel.formResult.collectAsState()
    LaunchedEffect(formResult) {
        when (val result = formResult) {
            is FormResult.Success -> {
                Toast.makeText(context, "Operação realizada com sucesso!", Toast.LENGTH_SHORT).show()
                // navController.previousBackStackEntry?.savedStateHandle?.set("colunaAtualizada", true)
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




    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, bottom = 50.dp)
        ) {
            CabecalhoAlternativo(
                titulo = if (isEditing) "Editar Coluna" else "Cadastrar Coluna",
                onVoltar = { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoFormulario(
                label = "Título",
                value = titulo,
                onValueChange = { titulo = it },
                singleLine = true
            )

            val canMarkAsCompleted = colunaState?.todasTarefasConcluidas ?: true
            val statusOptions = if (colunaState?.tarefas?.isNotEmpty() == true) {
                Status.values().toList().filter { it != Status.CONCLUIDA || canMarkAsCompleted || status == Status.CONCLUIDA }
            } else {
                Status.values().toList()
            }

            Spacer(modifier = Modifier.height(16.dp))

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




            Spacer(modifier = Modifier.weight(1f))

            BotoesFormulario(
                modifier = Modifier.navigationBarsPadding().padding (vertical =16.dp),
                onConfirm = {
                    if (titulo.isNotBlank()) {
                        val colunaParaSalvar = Coluna(
                            id = if (isEditing) colunaId!! else "",
                            titulo = titulo,
                            status = status,
                            ordem = colunaState?.ordem ?: 0,
                            tarefas = if (isEditing) colunaState?.tarefas ?: emptyList() else emptyList()
                        )
                        viewModel.salvarOuAtualizarColuna(quadroId, colunaParaSalvar)
                    } else {
                        Toast.makeText(context, "O título é obrigatório.", Toast.LENGTH_SHORT).show()
                    }
                },
                onDelete = if (isEditing) { { viewModel.excluirColuna(quadroId, colunaId!!) } } else null
            )
        }
    }
}
