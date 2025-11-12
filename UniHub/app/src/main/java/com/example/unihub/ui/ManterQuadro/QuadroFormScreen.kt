package com.example.unihub.ui.ManterQuadro


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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.data.repository.GrupoRepository
import com.example.unihub.data.repository.QuadroRepository
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import com.example.unihub.data.model.Contato
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.model.Grupo
import com.example.unihub.data.repository.ContatoResumo
import com.example.unihub.data.repository.Contatobackend
import com.example.unihub.data.repository.DisciplinaResumo
import com.example.unihub.data.repository.Grupobackend
import com.example.unihub.data.repository._disciplinabackend
import com.example.unihub.data.repository._quadrobackend
import com.example.unihub.data.model.Quadro
import com.example.unihub.Screen
import com.example.unihub.components.formatDateToLocale
import com.example.unihub.components.showLocalizedDatePicker
import java.util.Locale
import com.example.unihub.components.CampoBuscaJanela
import com.example.unihub.components.CabecalhoAlternativo

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
    val scrollState = rememberScrollState()

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

    val locale = remember { Locale("pt", "BR") }
    val showDatePicker = {
        showLocalizedDatePicker(context, uiState.prazo, locale, viewModel::onPrazoChange)
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 24.dp, end = 24.dp, bottom = 50.dp),
            ) {
            CabecalhoAlternativo(
                titulo = if (isEditing) "Editar Quadro" else "Cadastrar Quadro",
                onVoltar = { navController.popBackStack() }
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                CampoFormulario(
                    label = "Nome",
                    value = uiState.nome,
                    onValueChange = viewModel::onNomeChange,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                CampoCombobox(
                    label = "Disciplina (Opcional)",
                    options = uiState.disciplinasDisponiveis,
                    selectedOption = uiState.disciplinaSelecionada,
                    onOptionSelected = viewModel::onDisciplinaSelecionada,
                    optionToDisplayedString = { it.nome ?: "..." }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Integrante/Grupo (Opcional)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    if (uiState.integranteSelecionado == null) {
                        Button(
                            onClick = viewModel::onSelectionDialogShow,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF28A745)
                            )
                        ) { Text("Selecionar Integrante") }
                    } else {
                        val integranteSelecionado = uiState.integranteSelecionado
                        InputChip(
                            selected = true,
                            onClick = viewModel::onSelectionDialogShow,
                            label = {
                                if (integranteSelecionado is GrupoIntegranteUi) {
                                    val grupoDetalhes = uiState.integrantesDoQuadro.grupo
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            grupoDetalhes?.nome ?: integranteSelecionado.nome ?: "...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val membrosDoGrupo = grupoDetalhes?.membros.orEmpty()
                                        membrosDoGrupo.forEach { membro ->
                                            Text(
                                                text = membro,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (membrosDoGrupo.isEmpty()) {
                                            Text(
                                                text = "Nenhum integrante neste grupo.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else {
                                    Text(integranteSelecionado?.nome ?: "...")
                                }
                            },
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

                Spacer(modifier = Modifier.height(16.dp))

                CampoCombobox(
                    label = "Estado",
                    options = Estado.values().toList(),
                    selectedOption = uiState.estado,
                    onOptionSelected = viewModel::onEstadoChange,
                    optionToDisplayedString = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xE9CFE5D0), shape = RoundedCornerShape(8.dp))
                        .padding(vertical = 4.dp)
                ) {
                    CampoData(
                        label = "Prazo (Opcional)",
                        value = formatDateToLocale(uiState.prazo, locale),
                        onClick = showDatePicker
                    )

                    if (uiState.prazo != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { viewModel.onPrazoChange(null) }) {
                                Text("Remover data")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                BotoesFormulario(
                    modifier = Modifier.navigationBarsPadding().padding (vertical =16.dp),
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

    // --- NOVO: Estado para a busca ---
    var searchQuery by remember { mutableStateOf("") }

    // --- NOVO: Lógica de filtro ---
    val filteredContatos = remember(contatos, searchQuery) {
        contatos.filter {
            it.nome?.contains(searchQuery, ignoreCase = true) ?: false
        }
    }
    val filteredGrupos = remember(grupos, searchQuery) {
        grupos.filter {
            it.nome?.contains(searchQuery, ignoreCase = true) ?: false
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Selecionar Integrante") },
        text = {
            Column {
                // campo de busca agora
                CampoBuscaJanela(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Buscar por nome...",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

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
                        // --- USA A LISTA FILTRADA ---
                        if (filteredContatos.isEmpty()) {
                            item { Text("Nenhum contato encontrado.", modifier = Modifier.padding(16.dp)) }
                        }
                        items(filteredContatos) { contato ->
                            ListItem(
                                headlineContent = { Text(contato.nome ?: "") },
                                modifier = Modifier.clickable {
                                    onIntegranteSelected(ContatoIntegranteUi(contato.id, contato.nome))
                                }
                            )
                        }
                    } else { // Aba de Grupos
                        // --- USA A LISTA FILTRADA ---
                        if (filteredGrupos.isEmpty()) {
                            item { Text("Nenhum grupo encontrado.", modifier = Modifier.padding(16.dp)) }
                        }
                        items(filteredGrupos) { grupo ->
                            ListItem(
                                headlineContent = { Text(grupo.nome ?: "") },
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











// --- 1. DADOS FALSOS (MOCK DATA) ---
private val MOCK_DISCIPLINAS = listOf(
    DisciplinaResumo(id = 1, codigo = "CI068", nome = "Programação Orientada a Objetos", receberNotificacoes = true, isAtiva = true),
    DisciplinaResumo(id = 2, codigo = "CI062", nome = "Técnicas de Programação", receberNotificacoes = true, isAtiva = true),
    DisciplinaResumo(id = 3, codigo = "CE003", nome = "Estatística II", receberNotificacoes = true, isAtiva = true)
)
private val MOCK_CONTATOS = listOf(
    ContatoResumo(id = 101, nome = "Ana Beatriz", email = "ana.b@email.com", pendente = false, ownerId = 1L, registroId = 201),
    ContatoResumo(id = 102, nome = "Carlos Eduardo", email = "carlos.e@email.com", pendente = false, ownerId = 1L, registroId = 202)
)
// CÓDIGO CORRIGIDO
private val MOCK_GRUPOS = listOf(
    Grupo(id = 201, nome = "Grupo de Estudo de IA", membros = emptyList(), adminContatoId = null),
    Grupo(id = 202, nome = "Projeto TCC", membros = emptyList(), adminContatoId = null)
)

private val MOCK_QUADRO_PARA_EDICAO = Quadro(
    id = "quadro-id-123",
    nome = "Trabalho de Compiladores",
    estado = Estado.ATIVO,
    disciplinaId = 2,
    contatoId = 102,
    grupoId = null,
    colunas = emptyList(),
    dataFim = System.currentTimeMillis() + 86400000L * 10,
    donoId = 1L
)

// --- 2. IMPLEMENTAÇÕES FALSAS DOS REPOSITÓRIOS (COM CORREÇÃO) ---

private fun createFakeDisciplinaRepository() = DisciplinaRepository(object : _disciplinabackend {
    override suspend fun getDisciplinasResumoApi(): List<DisciplinaResumo> = MOCK_DISCIPLINAS
    override suspend fun getDisciplinaByIdApi(id: String): Disciplina? = null
    // --- FUNÇÕES QUE FALTAVAM ---
    override suspend fun addDisciplinaApi(disciplina: Disciplina) {} // Implementação vazia
    override suspend fun updateDisciplinaApi(id: Long, disciplina: Disciplina): Boolean = true
    override suspend fun deleteDisciplinaApi(id: Long): Boolean = true
})

private fun createFakeContatoRepository() = ContatoRepository(object : Contatobackend {
    override suspend fun getContatoResumoApi(): List<ContatoResumo> = MOCK_CONTATOS
    override suspend fun getContatoByIdApi(id: String): Contato? = null

    // --- FUNÇÕES QUE FALTAVAM ---


    override suspend fun getConvitesPendentesPorEmail(email: String): List<ContatoResumo> {
        // This is the function causing the error. Return an empty list for the mock.
        return emptyList()
    }
    override suspend fun addContatoApi(contato: Contato) {} // Implementação vazia
    override suspend fun updateContatoApi(id: Long, contato: Contato): Boolean = true
    override suspend fun deleteContatoApi(id: Long): Boolean = true
    override suspend fun acceptInvitation(id: Long) { /* no-op para preview */ }
    override suspend fun rejectInvitation(id: Long) { /* no-op para preview */ }
})

private fun createFakeGrupoRepository() = GrupoRepository(object : Grupobackend {
    override suspend fun getGrupoApi(): List<Grupo> = MOCK_GRUPOS
    override suspend fun getGrupoByIdApi(id: String): Grupo? = null
    // --- FUNÇÕES QUE FALTAVAM ---
    override suspend fun addGrupoApi(grupo: Grupo) {} // Implementação vazia
    override suspend fun updateGrupoApi(id: Long, grupo: Grupo): Boolean = true
    override suspend fun deleteGrupoApi(id: Long): Boolean = true
    override suspend fun leaveGrupoApi(id: Long): Boolean = true
})

private fun createFakeQuadroRepository() = QuadroRepository(object : _quadrobackend {
    override suspend fun getQuadrosApi(): List<Quadro> = emptyList()
    override suspend fun getQuadroByIdApi(id: String): Quadro? = if (id == "quadro-id-123") MOCK_QUADRO_PARA_EDICAO else null
    // --- FUNÇÕES QUE FALTAVAM ---
    override suspend fun addQuadroApi(quadro: Quadro) {} // Implementação vazia
    override suspend fun updateQuadroApi(id: Long, quadro: Quadro): Boolean = true
    override suspend fun deleteQuadroApi(id: Long): Boolean = true
})

// --- 3. FÁBRICA DE VIEWMODEL PARA A PREVIEW ---
private class PreviewViewModelFactory : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuadroFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuadroFormViewModel(
                quadroRepository = createFakeQuadroRepository(),
                disciplinaRepository = createFakeDisciplinaRepository(),
                contatoRepository = createFakeContatoRepository(),
                grupoRepository = createFakeGrupoRepository()
            ) as T
        }
        throw IllegalArgumentException("Classe de ViewModel desconhecida para a preview")
    }
}

// --- 4. AS PREVIEWS ---
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Preview(showBackground = true, name = "Modo de Criação")
@Composable
fun QuadroFormScreen_CreateMode_Preview() {
    QuadroFormScreen(
        navController = rememberNavController(),
        quadroId = null,
        viewModelFactory = PreviewViewModelFactory()
    )
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Preview(showBackground = true, name = "Modo de Edição")
@Composable
fun QuadroFormScreen_EditMode_Preview() {
    QuadroFormScreen(
        navController = rememberNavController(),
        quadroId = "quadro-id-123",
        viewModelFactory = PreviewViewModelFactory()
    )
}