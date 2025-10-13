package com.example.unihub.ui.ManterQuadro

import android.app.DatePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.unihub.components.*
import com.example.unihub.data.model.Estado
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.rememberNavController
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.data.repository.GrupoRepository
import com.example.unihub.data.repository.QuadroRepository
import kotlinx.coroutines.flow.flowOf
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.unihub.data.model.Contato
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.model.Grupo
import com.example.unihub.data.model.HorarioAula
import com.example.unihub.data.repository.ContatoResumo
import com.example.unihub.data.repository.Contatobackend
import com.example.unihub.data.repository.DisciplinaResumo
import com.example.unihub.data.repository.Grupobackend
import com.example.unihub.data.repository._disciplinabackend
import com.example.unihub.data.repository._quadrobackend
import java.time.LocalDate
import com.example.unihub.data.model.Quadro
import com.example.unihub.Screen

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuadroFormScreen(
    navController: NavHostController,
    quadroId: String? = null,
    viewModelFactory: ViewModelProvider.Factory
) {
    val viewModel: QuadroFormViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val formResult by viewModel.formResult.collectAsState()
    val context = LocalContext.current
    val isEditing = quadroId != null

    LaunchedEffect(key1 = Unit) {
        viewModel.carregarDados(quadroId)
    }

    //novo
    LaunchedEffect(formResult) {
        when (val result = formResult) {
            is FormResult.Success -> {
                Toast.makeText(context, "Operação realizada com sucesso!", Toast.LENGTH_SHORT).show()
                navController.previousBackStackEntry?.savedStateHandle?.set("refreshQuadros", true)
                // pra voltar duas telas
                navController.popBackStack(Screen.ListarQuadros.route, false)
            }
            is FormResult.Error -> {
                Toast.makeText(context, "Erro: ${result.message}", Toast.LENGTH_LONG).show()
                viewModel.resetFormResult()
            }
            else -> {}
        }
    }

    if (uiState.isSelectionDialogVisible) {
        SelecaoIntegranteDialog(
            contatos = uiState.contatosDisponiveis,
            grupos = uiState.gruposDisponiveis,
            onDismissRequest = viewModel::onSelectionDialogDismiss,
            onIntegranteSelected = viewModel::onIntegranteSelecionado
        )
    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val showDatePicker = {
        val calendar = Calendar.getInstance().apply { timeInMillis = uiState.prazo }
        DatePickerDialog(context, { _, year, month, day ->
            calendar.set(year, month, day)
            viewModel.onPrazoChange(calendar.timeInMillis)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 50.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header(
                titulo = if (isEditing) "Editar Quadro" else "Cadastrar Quadro",
                onVoltar = { navController.popBackStack() }
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                CampoFormulario(label = "Nome", value = uiState.nome, onValueChange = viewModel::onNomeChange)

                CampoCombobox(
                    label = "Disciplina (Opcional)",
                    options = uiState.disciplinasDisponiveis,
                    selectedOption = uiState.disciplinaSelecionada,
                    onOptionSelected = viewModel::onDisciplinaSelecionada,
                    optionToDisplayedString = { it.nome ?: "..." }
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Integrante/Grupo (Opcional)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    if (uiState.integranteSelecionado == null) {
                        Button(
                            onClick = viewModel::onSelectionDialogShow,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) { Text("Selecionar Integrante") }
                    } else {
                        InputChip(
                            selected = true,
                            onClick = viewModel::onSelectionDialogShow,
                            label = { Text(uiState.integranteSelecionado?.nome ?: "...") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remover integrante",
                                    modifier = Modifier.clickable { viewModel.onIntegranteSelecionado(null) }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                CampoCombobox(
                    label = "Estado",
                    options = Estado.values().toList(),
                    selectedOption = uiState.estado,
                    onOptionSelected = viewModel::onEstadoChange,
                    optionToDisplayedString = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
                )

                CampoData(label = "Prazo (Data de Fim)", value = dateFormat.format(Date(uiState.prazo)), onClick = { showDatePicker() })

                Spacer(modifier = Modifier.weight(1f))

                BotoesFormulario(
                    onConfirm = viewModel::salvarOuAtualizarQuadro,
                    onDelete = if (isEditing) { { viewModel.excluirQuadro(quadroId!!) } } else null
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelecaoIntegranteDialog(
    contatos: List<ContatoResumoUi>,
    grupos: List<GrupoResumoUi>,
    onDismissRequest: () -> Unit,
    onIntegranteSelected: (IntegranteUi) -> Unit
) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Contatos", "Grupos")

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Selecionar Integrante") },
        text = {
            Column {
                TabRow(selectedTabIndex = tabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = tabIndex == index,
                            onClick = { tabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp).fillMaxWidth()) {
                    if (tabIndex == 0) { // Aba de Contatos
                        items(contatos) { contato ->
                            // --- CORREÇÃO FINAL AQUI ---
                            ListItem(
                                headlineContent = { Text(contato.nome ?: "") }, // DE: headlineText
                                modifier = Modifier.clickable {
                                    onIntegranteSelected(ContatoIntegranteUi(contato.id, contato.nome))
                                }
                            )
                        }
                    } else { // Aba de Grupos
                        items(grupos) { grupo ->
                            // --- CORREÇÃO FINAL AQUI ---
                            ListItem(
                                headlineContent = { Text(grupo.nome ?: "") }, // DE: headlineText
                                modifier = Modifier.clickable {
                                    onIntegranteSelected(GrupoIntegranteUi(grupo.id, grupo.nome))
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}


















