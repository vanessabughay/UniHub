package com.example.unihub.ui.ManterQuadro

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.unihub.components.BotoesFormulario
import com.example.unihub.components.CampoCombobox
import com.example.unihub.components.CampoData
import com.example.unihub.components.CampoFormulario
import com.example.unihub.components.Header
import com.example.unihub.data.model.Estado
import com.example.unihub.data.model.QuadroDePlanejamento
import com.example.unihub.data.repository.QuadroRepository
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.unihub.data.api.QuadroApi

@Composable
fun QuadroFormScreen(
    navController: NavHostController,
    quadroId: String? = null,
    viewModelFactory: ViewModelProvider.Factory
) {
    val viewModel: QuadroFormViewModel = viewModel(factory = viewModelFactory)

    val context = LocalContext.current
    val isEditing = quadroId != null
    val quadroState by viewModel.quadro.collectAsState()

    var nome by remember { mutableStateOf("") }
    var disciplina by remember { mutableStateOf("") }
    var integrantes by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf(Estado.ATIVO) }
    var prazo by remember { mutableStateOf(System.currentTimeMillis()) }
    var dataInicio by remember { mutableStateOf(System.currentTimeMillis()) }
    var donoId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(key1 = quadroId) {
        if (isEditing) {
            viewModel.carregarQuadro(quadroId!!)
        }
    }

    LaunchedEffect(key1 = quadroState) {
        if (isEditing) {
            quadroState?.let { loadedQuadro ->
                nome = loadedQuadro.nome
                disciplina = loadedQuadro.disciplina ?: ""
                integrantes = loadedQuadro.integrantes?.joinToString(", ") ?: ""
                estado = loadedQuadro.estado
                prazo = loadedQuadro.dataFim ?: System.currentTimeMillis()
                dataInicio = loadedQuadro.dataInicio
                donoId = loadedQuadro.donoId
            }
        }
    }

    val formResult by viewModel.formResult.collectAsState()
    LaunchedEffect(formResult) {
        when (val result = formResult) {
            is FormResult.Success -> {
                Toast.makeText(context, "Operação realizada com sucesso!", Toast.LENGTH_SHORT).show()
                navController.previousBackStackEntry?.savedStateHandle?.set("refreshQuadros", true)
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
                titulo = if (isEditing) "Editar Quadro" else "Cadastrar Quadro",
                onVoltar = { navController.popBackStack() }
            )

            CampoFormulario(label = "Nome", value = nome, onValueChange = { nome = it })
            CampoFormulario(label = "Disciplina (Opcional)", value = disciplina, onValueChange = { disciplina = it })
            CampoFormulario(label = "Integrantes/Grupo", value = integrantes, onValueChange = { integrantes = it })

            CampoCombobox(
                label = "Estado",
                options = Estado.values().toList(),
                selectedOption = estado,
                onOptionSelected = { estado = it },
                optionToDisplayedString = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
            )

            CampoData(label = "Prazo (Data de Fim)", value = dateFormat.format(Date(prazo)), onClick = { showDatePicker() })

            Spacer(modifier = Modifier.weight(1f))

            BotoesFormulario(
                onConfirm = {
                    if (nome.isNotBlank()) {
                        val integrantesList = integrantes.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        val quadroParaSalvar = QuadroDePlanejamento(
                            id = if (isEditing) quadroId else null,
                            nome = nome,
                            disciplina = disciplina.ifBlank { null },
                            integrantes = if (integrantesList.isEmpty()) null else integrantesList,
                            estado = estado,
                            dataInicio = dataInicio,
                            dataFim = prazo,
                            donoId = donoId

                        )
                        viewModel.salvarOuAtualizarQuadro(quadroParaSalvar)
                    } else {
                        Toast.makeText(context, "O nome do quadro é obrigatório.", Toast.LENGTH_SHORT).show()
                    }
                },
                onDelete = if (isEditing) { { viewModel.excluirQuadro(quadroId!!) } } else null
            )
        }
    }
}








// Repositório falso para simular chamadas de API.
class FakeQuadroRepository : QuadroRepository(object : QuadroApi {
    override suspend fun getQuadros(): List<QuadroDePlanejamento> {
        return emptyList()
    }

    override suspend fun getQuadroById(quadroId: String): QuadroDePlanejamento {
        // Retorna um objeto de quadro de exemplo para a prévia.
        return QuadroDePlanejamento(
            id = quadroId,
            nome = "Quadro de Exemplo",
            disciplina = "Programação Móvel",
            integrantes = listOf("João", "Maria"),
            estado = Estado.ATIVO,
            colunas = emptyList(),
            dataInicio = System.currentTimeMillis(),
            dataFim = System.currentTimeMillis() + 86400000L,
            donoId = 1L
        )
    }

    override suspend fun addQuadro(quadro: QuadroDePlanejamento): QuadroDePlanejamento {
        return quadro
    }

    override suspend fun updateQuadro(quadroId: String, quadro: QuadroDePlanejamento): QuadroDePlanejamento {
        return quadro
    }

    override suspend fun deleteQuadro(quadroId: String) {
        // Nada a ser feito aqui.
    }
})

// Fábrica de ViewModel falsa para injetar o repositório falso.
class FakeQuadroFormViewModelFactory : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuadroFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuadroFormViewModel(FakeQuadroRepository()) as T
        }
        throw IllegalArgumentException("Classe de ViewModel desconhecida")
    }
}

// Prévia para o modo de cadastro de quadro.
@Preview(showBackground = true)
@Composable
fun QuadroFormScreenPreview() {
    QuadroFormScreen(
        navController = rememberNavController(),
        quadroId = null, // null para o modo de cadastro
        viewModelFactory = FakeQuadroFormViewModelFactory()
    )
}

// Prévia para o modo de edição de quadro.
@Preview(showBackground = true)
@Composable
fun QuadroFormScreenEditingPreview() {
    QuadroFormScreen(
        navController = rememberNavController(),
        quadroId = "id-do-quadro-exemplo", // id para o modo de edição
        viewModelFactory = FakeQuadroFormViewModelFactory()
    )
}