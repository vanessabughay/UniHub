package com.example.unihub.ui.VisualizarDisciplina

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.data.model.Ausencia
import com.example.unihub.data.model.Avaliacao
import com.example.unihub.ui.ListarAvaliacao.DisciplinaGrupoCard
import com.example.unihub.ui.ListarAvaliacao.ListarAvaliacaoViewModel
import com.example.unihub.ui.ListarAvaliacao.ListarAvaliacaoViewModelFactory
import java.time.format.DateTimeFormatter

// Cores dos cards e botões
private val AvaliacoesCardColor = Color(0xFFE0E1F8)       // igual ao da lista de avaliações
private val AusenciasCardColor  = Color(0xFFF3E4F8)       // fundo do card de ausências
private val AusenciasBtnColor   = Color(0xFFE1C2F0)       // um pouco mais escura que o card
private val PesoNotasColor      = Color(0xFFD8ECDF)       // fundo do card de peso das notas
private val PesoNotasBtnColor = Color(0xFFBED0C4)       // fundo do card de peso das notas

data class OpcaoDisciplina(
    val title: String,
    val icon: ImageVector,
    val background: Color,
    val onClick: () -> Unit
)

@Composable
fun BotaoOpcao(item: OpcaoDisciplina) {
    Button(
        onClick = item.onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = item.background,
            contentColor = Color.Black
        ),
        contentPadding = PaddingValues(vertical = 20.dp, horizontal = 16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(imageVector = item.icon, contentDescription = null, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = item.title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun AusenciasCard(
    expanded: Boolean,
    ausencias: List<Ausencia>,
    onToggle: () -> Unit,
    onAdd: () -> Unit,                   // botão de rodapé
    onItemClick: (Ausencia) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AusenciasCardColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Cabeçalho — "+" apenas expande/retrai
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Ausências", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onToggle) {
                    Icon(Icons.Default.Add, contentDescription = "Expandir Ausências")
                }
            }

            if (expanded) {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                if (ausencias.isEmpty()) {
                    Text(
                        text = "Nenhuma ausência registrada",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else {
                    ausencias.forEach { aus ->
                        Text(
                            text = aus.data.format(formatter) + (aus.categoria?.let { " - $it" } ?: ""),
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { onItemClick(aus) }
                        )
                    }
                }

                // Botão de rodapé: ADICIONAR AUSÊNCIA
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onAdd,
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = AusenciasBtnColor,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Adicionar ausência")
                    }
                }
            }
        }
    }
}

@Composable
fun AvaliacoesCard(
    expanded: Boolean,
    disciplinaNome: String,
    avaliacoes: List<Avaliacao>,
    onToggle: () -> Unit,
    onAddAvaliacaoParaDisciplina: () -> Unit,
    onExcluir: (Avaliacao) -> Unit,
    onToggleConcluida: (Avaliacao, Boolean) -> Unit,
    onEditarNotaClick: (Avaliacao) -> Unit,
    onEditarAvaliacao: (Avaliacao) -> Unit,
    isLoading: Boolean
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AvaliacoesCardColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Cabeçalho — "+" apenas expande/retrai
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Outlined.AddTask, contentDescription = null, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Avaliações", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onToggle) {
                    Icon(Icons.Default.Add, contentDescription = "Expandir Avaliações")
                }
            }

            if (expanded) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                } else if (avaliacoes.isEmpty()) {
                    Text(
                        text = "Nenhuma avaliação cadastrada para esta disciplina.",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    // Rodapé com botão "Adicionar avaliação" mesmo sem itens
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onAddAvaliacaoParaDisciplina) {
                            Text("Adicionar avaliação")
                        }
                    }
                } else {
                    // Reuso da UI da lista (com botão "Adicionar avaliação")
                    DisciplinaGrupoCard(
                        nome = disciplinaNome,
                        podeAdicionar = true,
                        avaliacoes = avaliacoes.sortedBy { it.descricao ?: "" },
                        onAddClick = onAddAvaliacaoParaDisciplina,   // << navegação direta
                        onAvaliacaoClick = onEditarAvaliacao,
                        onExcluirClick = onExcluir,
                        onToggleConcluida = onToggleConcluida,
                        onEditarNotaClick = onEditarNotaClick
                    )
                }
            }
        }
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun VisualizarDisciplinaScreen(
    disciplinaId: String?,
    onVoltar: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToAnotacoes: (String) -> Unit,
    onNavigateToAusencias: (String, String?) -> Unit,

    // >>> NOVOS CALLBACKS DE NAVEGAÇÃO <<<
    onNavigateToAddAvaliacaoParaDisciplina: (String) -> Unit,
    onNavigateToManterAvaliacao: (String) -> Unit,

    viewModel: VisualizarDisciplinaViewModel
) {
    val context = LocalContext.current
    val disciplina by viewModel.disciplina.collectAsStateWithLifecycle()
    val erro by viewModel.erro.collectAsStateWithLifecycle()
    val ausencias by viewModel.ausencias.collectAsStateWithLifecycle()
    var expandAusencias by remember { mutableStateOf(false) }

    // Avaliações (reutilizando a ViewModel da lista)
    var expandAvaliacoes by remember { mutableStateOf(false) }
    val avaliacoesVM: ListarAvaliacaoViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ListarAvaliacaoViewModelFactory
    )
    val avaliacoesAll by avaliacoesVM.avaliacoes.collectAsState()
    val isLoadingAval by avaliacoesVM.isLoading.collectAsState()
    val errorAval by avaliacoesVM.errorMessage.collectAsState()

    // Diálogos de avaliação
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }
    var avaliacaoParaExcluir by remember { mutableStateOf<Avaliacao?>(null) }
    var avaliacaoParaConcluir by remember { mutableStateOf<Avaliacao?>(null) }
    var avaliacaoParaReativar by remember { mutableStateOf<Avaliacao?>(null) }
    var notaDialogAvaliacao by remember { mutableStateOf<Avaliacao?>(null) }
    var notaTemp by remember { mutableStateOf("") }

    LaunchedEffect(disciplinaId) {
        disciplinaId?.let { viewModel.loadDisciplina(it) }
        avaliacoesVM.loadAvaliacao()
    }

    LaunchedEffect(errorAval) {
        errorAval?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            avaliacoesVM.clearErrorMessage()
        }
    }

    erro?.let {
        Toast.makeText(context, "Erro: $it", Toast.LENGTH_LONG).show()
    }

    disciplina?.let { disc ->
        val avaliacoesDaDisciplina = remember(avaliacoesAll, disc.id) {
            avaliacoesAll.filter { it.disciplina?.id == disc.id }
        }

        val opcoes = listOf(
            OpcaoDisciplina("Informações da disciplina", Icons.Outlined.Info, Color(0xFFD7EFF5)) {
                onNavigateToEdit(disc.id.toString())
            },

            OpcaoDisciplina("Peso das Notas", Icons.Outlined.StarOutline, PesoNotasColor) {
                Toast.makeText(context, "Abrir Notas", Toast.LENGTH_SHORT).show()
            },
            OpcaoDisciplina("Minhas anotações", Icons.AutoMirrored.Outlined.Notes, Color(0xFFF8F1E1)) {
                disc.id?.let { onNavigateToAnotacoes(it.toString()) }
            }
        )

        // === Diálogo: Excluir avaliação
        if (showConfirmDeleteDialog && avaliacaoParaExcluir != null) {
            val av = avaliacaoParaExcluir!!
            AlertDialog(
                onDismissRequest = { showConfirmDeleteDialog = false; avaliacaoParaExcluir = null },
                title = { Text("Confirmar Exclusão") },
                text = { Text("Deseja mesmo excluir a avaliação \"${av.descricao}\"?") },
                confirmButton = {
                    TextButton(onClick = {
                        avaliacoesVM.deleteAvaliacao(av.id!!.toString()) { sucesso ->
                            if (sucesso) Toast.makeText(context, "Avaliação excluída!", Toast.LENGTH_SHORT).show()
                        }
                        showConfirmDeleteDialog = false
                        avaliacaoParaExcluir = null
                    }) { Text("EXCLUIR") }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDeleteDialog = false; avaliacaoParaExcluir = null }) {
                        Text("CANCELAR")
                    }
                }
            )
        }

        // === Diálogo: Concluir/Reativar
        avaliacaoParaConcluir?.let { av ->
            AlertDialog(
                onDismissRequest = { avaliacaoParaConcluir = null },
                title = { Text("Confirmar") },
                text = { Text("Deseja mesmo concluir essa avaliação?") },
                confirmButton = {
                    TextButton(onClick = {
                        avaliacoesVM.toggleConcluida(av, true) { ok ->
                            if (ok) Toast.makeText(context, "Avaliação concluída!", Toast.LENGTH_SHORT).show()
                        }
                        avaliacaoParaConcluir = null
                    }) { Text("CONCLUIR AVALIAÇÃO") }
                },
                dismissButton = { TextButton(onClick = { avaliacaoParaConcluir = null }) { Text("CANCELAR") } }
            )
        }
        avaliacaoParaReativar?.let { av ->
            AlertDialog(
                onDismissRequest = { avaliacaoParaReativar = null },
                title = { Text("Confirmar") },
                text = { Text("Deseja mesmo reativar essa avaliação?") },
                confirmButton = {
                    TextButton(onClick = {
                        avaliacoesVM.toggleConcluida(av, false) { ok ->
                            if (ok) Toast.makeText(context, "Avaliação reativada!", Toast.LENGTH_SHORT).show()
                        }
                        avaliacaoParaReativar = null
                    }) { Text("REATIVAR AVALIAÇÃO") }
                },
                dismissButton = { TextButton(onClick = { avaliacaoParaReativar = null }) { Text("CANCELAR") } }
            )
        }

        // === Diálogo: Nota
        notaDialogAvaliacao?.let { av ->
            AlertDialog(
                onDismissRequest = { notaDialogAvaliacao = null },
                title = { Text("Definir nota") },
                text = {
                    Column {
                        Text("Avaliação: " + (av.descricao ?: ""))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notaTemp,
                            onValueChange = { notaTemp = it },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            label = { Text("Nota") },
                            placeholder = { Text("Ex.: 8.5") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val valor = notaTemp.trim().takeIf { it.isNotEmpty() }?.replace(',', '.')?.toDoubleOrNull()
                        avaliacoesVM.updateNota(av, valor) { ok ->
                            if (ok) {
                                Toast.makeText(context, "Nota salva!", Toast.LENGTH_SHORT).show()
                                notaDialogAvaliacao = null
                            }
                        }
                    }) { Text("SALVAR") }
                },
                dismissButton = { TextButton(onClick = { notaDialogAvaliacao = null }) { Text("CANCELAR") } }
            )
        }

        Scaffold(
            topBar = {
                CabecalhoAlternativo(
                    titulo = disc.nome.orEmpty(),
                    onVoltar = onVoltar
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    BotaoOpcao(item = opcoes.first())
                }
                item {
                    AusenciasCard(
                        expanded = expandAusencias,
                        ausencias = ausencias,
                        onToggle = { expandAusencias = !expandAusencias },
                        onAdd = { onNavigateToAusencias(disc.id.toString(), null) },   // botão de rodapé
                        onItemClick = { aus -> onNavigateToAusencias(disc.id.toString(), aus.id?.toString()) }
                    )
                }
                item {
                    AvaliacoesCard(
                        expanded = expandAvaliacoes,
                        onToggle = { expandAvaliacoes = !expandAvaliacoes },
                        disciplinaNome = disc.nome.orEmpty(),
                        avaliacoes = avaliacoesDaDisciplina,
                        onAddAvaliacaoParaDisciplina = {
                            onNavigateToAddAvaliacaoParaDisciplina(disc.id.toString())
                        },
                        onExcluir = { av ->
                            avaliacaoParaExcluir = av
                            showConfirmDeleteDialog = true
                        },
                        onToggleConcluida = { av, marcado ->
                            if (marcado) avaliacaoParaConcluir = av else avaliacaoParaReativar = av
                        },
                        onEditarNotaClick = { av ->
                            notaTemp = av.nota?.toString() ?: ""
                            notaDialogAvaliacao = av
                        },
                        onEditarAvaliacao = { av ->
                            av.id?.let { onNavigateToManterAvaliacao(it.toString()) }
                        },
                        isLoading = isLoadingAval
                    )
                }
                items(opcoes.drop(1)) { opcao ->
                    BotaoOpcao(item = opcao)
                }
            }
        }
    }
}
