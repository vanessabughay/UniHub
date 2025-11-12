package com.example.unihub.ui.ListarQuadros

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.unihub.components.CampoBusca
import com.example.unihub.data.model.Quadro
import com.example.unihub.data.model.Estado
import com.example.unihub.data.model.Contato
import com.example.unihub.data.model.Grupo
import com.example.unihub.data.repository.GrupoRepository
import com.example.unihub.data.repository.Grupobackend
import com.example.unihub.data.repository.QuadroRepository
import com.example.unihub.data.repository._quadrobackend
import androidx.compose.ui.tooling.preview.Preview
import com.example.unihub.Screen
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.ui.Shared.ZeroInsets



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListarQuadrosScreen(
    navController: NavHostController,
    viewModelFactory: ViewModelProvider.Factory
) {
    val viewModel: ListarQuadrosViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var secaoAtivaExpandida by remember { mutableStateOf(true) }
    var secaoInativaExpandida by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }

    val quadrosFiltrados = remember(searchQuery, uiState.quadros) {
        if (searchQuery.isBlank()) {
            uiState.quadros
        } else {
            uiState.quadros.filter {
                it.nome.contains(searchQuery, ignoreCase = true) ||
                        (it.id?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    val quadrosAtivos = remember(quadrosFiltrados) {
        quadrosFiltrados.filter { it.estado == Estado.ATIVO }
    }
    val quadrosInativos = remember(quadrosFiltrados) {
        quadrosFiltrados.filter { it.estado == Estado.INATIVO }
    }


    LaunchedEffect(Unit) {
        viewModel.carregarQuadros()
    }

    val refreshQuadrosState = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refreshQuadros", false)
        ?.collectAsState()

    LaunchedEffect(refreshQuadrosState?.value) {
        if (refreshQuadrosState?.value == true) {
            viewModel.carregarQuadros()
            navController.currentBackStackEntry?.savedStateHandle?.set("refreshQuadros", false)
        }
    }

    uiState.error?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { navController.navigate(Screen.ManterQuadro.createRoute(id = null)) },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text("Novo Quadro", modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        },
        contentWindowInsets = ZeroInsets
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            CabecalhoAlternativo(
                titulo = "Meus Quadros",
                onVoltar = { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SearchSection(
                searchQuery = searchQuery,
                onSearchQueryChanged = { searchQuery = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 50.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (quadrosAtivos.isNotEmpty()) {
                        item {
                            TituloDeSecao(
                                titulo = "Quadros ativos",
                                setaAbaixo = secaoAtivaExpandida,
                                onClick = { secaoAtivaExpandida = !secaoAtivaExpandida }
                            )
                        }
                        items(quadrosAtivos, key = { it.id ?: it.nome }) { quadro ->
                            AnimatedVisibility(visible = secaoAtivaExpandida) {
                                QuadroCard(
                                    quadro = quadro,
                                    onSingleClick = {
                                        quadro.id?.let { id ->
                                            navController.navigate(Screen.VisualizarQuadro.createRoute(id))
                                        }
                                    }
                                )
                            }
                        }
                    }

                    if (quadrosInativos.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            TituloDeSecao(
                                titulo = "Quadros inativos",
                                setaAbaixo = secaoInativaExpandida,
                                onClick = { secaoInativaExpandida = !secaoInativaExpandida }
                            )
                        }
                        items(quadrosInativos, key = { it.id ?: it.nome }) { quadro ->
                            AnimatedVisibility(visible = secaoInativaExpandida) {
                                QuadroCard(
                                    quadro = quadro,
                                    onSingleClick = {
                                        quadro.id?.let { id ->
                                            navController.navigate(Screen.VisualizarQuadro.createRoute(id))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSection(searchQuery: String, onSearchQueryChanged: (String) -> Unit) {
    CampoBusca(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        placeholder = "Buscar quadro...",
        modifier = Modifier.fillMaxWidth()
    )
}

// Composable para o título de seção, adaptado do seu modelo
@Composable
private fun TituloDeSecao(titulo: String, setaAbaixo: Boolean, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (setaAbaixo) Icons.Outlined.ExpandMore else Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = titulo,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 10.dp),
            thickness = DividerDefaults.Thickness,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

@Composable
private fun QuadroCard(
    quadro: Quadro,
    onSingleClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSingleClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = quadro.nome,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Abrir quadro",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}











// --- Classes de Mock e Previews ---

// Repositório falso para simular as chamadas de API do Quadro.
private fun previewQuadroRepositoryListar() = QuadroRepository(object : _quadrobackend {
    override suspend fun getQuadrosApi(): List<Quadro> {
        return listOf(
            Quadro(
                id = "quadro-1",
                nome = "Projeto DAC",
                estado = Estado.ATIVO
            ),
            Quadro(
                id = "quadro-2",
                nome = "Projeto Front-end",
                estado = Estado.ATIVO
            ),
            Quadro(
                id = "quadro-3",
                nome = "Projeto TCC 1",
                estado = Estado.INATIVO
            )
        )
    }

    override suspend fun getQuadroByIdApi(id: String): Quadro? {
        return Quadro(id = id, nome = "Quadro $id")
    }

    override suspend fun addQuadroApi(quadro: Quadro) {}

    override suspend fun updateQuadroApi(id: Long, quadro: Quadro): Boolean = true

    override suspend fun deleteQuadroApi(id: Long): Boolean = true
})

private fun previewGrupoRepositoryListar() = GrupoRepository(object : Grupobackend {
    override suspend fun getGrupoApi(): List<Grupo> = emptyList()

    override suspend fun getGrupoByIdApi(id: String): Grupo? = Grupo(
        id = id.toLongOrNull(),
        nome = "Grupo $id",
        membros = listOf(
            Contato(
                id = 1L,
                nome = "Membro",
                email = "membro@example.com",
                pendente = false,
                idContato = 1L,
                ownerId = 1L
            )
        ),
        adminContatoId = 1L,
        ownerId = 1L
    )

    override suspend fun addGrupoApi(grupo: Grupo) {}

    override suspend fun updateGrupoApi(id: Long, grupo: Grupo): Boolean = true

    override suspend fun deleteGrupoApi(id: Long): Boolean = true

    override suspend fun leaveGrupoApi(id: Long): Boolean = true
})

class FakeListarQuadrosViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListarQuadrosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Instancia o ViewModel com o repositório falso
            return ListarQuadrosViewModel(
                previewQuadroRepositoryListar(),
                previewGrupoRepositoryListar()
            ) as T
        }
        throw IllegalArgumentException("Classe de ViewModel desconhecida")
    }
}



@Preview(showBackground = true)
@Composable
fun ListarQuadrosScreenPreview() {
    ListarQuadrosScreen(
        navController = rememberNavController(),
        viewModelFactory = FakeListarQuadrosViewModelFactory()
    )
}