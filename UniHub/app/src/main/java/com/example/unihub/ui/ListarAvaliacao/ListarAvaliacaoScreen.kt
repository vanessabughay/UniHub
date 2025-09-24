package com.example.unihub.ui.ListarAvaliacao

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.animation.AnimatedVisibility // Certifique-se desta importação
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown // NOVO
import androidx.compose.material.icons.filled.KeyboardArrowUp // NOVO
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.components.SearchBox
import com.example.unihub.data.model.Avaliacao


val CardDefaultBackgroundColor = Color(0xFFF0F0F0)


@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun ListarAvaliacaoScreen(
    viewModel: ListarAvaliacaoViewModel = viewModel(factory = ListarAvaliacaoViewModelFactory),
    onAddAvaliacaoParaDisciplina: (disciplinaId: String) -> Unit,
    onAddAvaliacaoGeral: () -> Unit, // novo
    onVoltar: () -> Unit,
    onNavigateToManterAvaliacao: (avaliacaoId: String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val avaliacoesState by viewModel.avaliacoes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var avaliacaoExpandidoId by remember { mutableStateOf<Long?>(null) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) } // MODIFICADO: Nome simplificado
    var avaliacaoParaExcluir by remember { mutableStateOf<Avaliacao?>(null) } // MODIFICADO: Usaremos este

    var searchQuery by remember { mutableStateOf("") }


    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadAvaliacao()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val avaliacoesComIdValido by remember(avaliacoesState) {
        derivedStateOf {
            avaliacoesState.filter { it.id != null }
        }
    }

    val avaliacoesFiltrados by remember(searchQuery, avaliacoesComIdValido) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                avaliacoesComIdValido
            } else {
                val q = searchQuery.trim()
                avaliacoesComIdValido.filter { avaliacao ->
                    val descOk = avaliacao.descricao?.contains(q, ignoreCase = true) == true
                    val idOk   = avaliacao.id?.toString()?.contains(q, ignoreCase = true) == true
                    val discOk = avaliacao.disciplina?.nome?.contains(q, ignoreCase = true) == true
                    descOk || idOk || discOk
                }
            }
        }
    }

    // Diálogo de Confirmação de Exclusão (agora usando showConfirmDeleteDialog e avaliacaoParaExcluir)
    if (showConfirmDeleteDialog && avaliacaoParaExcluir != null) {
        val avaliacaoParaExcluirAtual = avaliacaoParaExcluir!! // Sabemos que não é nulo aqui
        AlertDialog(
            onDismissRequest = {
                showConfirmDeleteDialog = false
                avaliacaoParaExcluir = null // Limpa ao fechar
            },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Deseja realmente excluir a avaliação \"${avaliacaoParaExcluirAtual.descricao}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // avaliacaoParaExcluirAtual.id é não nulo porque vem de um item filtrado
                        viewModel.deleteAvaliacao(avaliacaoParaExcluirAtual.id!!.toString()) { sucesso ->
                            if (sucesso) {
                                Toast.makeText(context, "Avaliação excluída!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showConfirmDeleteDialog = false
                        avaliacaoParaExcluir = null
                    }
                ) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmDeleteDialog = false
                    avaliacaoParaExcluir = null
                }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            CabecalhoAlternativo(
                titulo = "Avaliações",
                onVoltar = onVoltar,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAvaliacaoGeral,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Avaliação")
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SearchBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        ),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    Text("Buscar por nome da avaliação", color = Color.Gray, fontSize = 16.sp)
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    avaliacoesComIdValido.isEmpty() && !isLoading && errorMessage == null && searchQuery.isBlank() -> {
                        Box( modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp) ) {
                            Text("Nenhuma avaliação cadastrada.", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    // A condição para "Nenhum avaliacao encontrado para a busca" usa avaliacoesFiltrados
                    avaliacoesFiltrados.isEmpty() && searchQuery.isNotBlank() && !isLoading && errorMessage == null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nenhuma avaliação encontrada para \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    else -> {
                        if (isLoading && avaliacoesComIdValido.isNotEmpty()) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                            )
                        }

                        // agrupa por disciplinaId; -1L = sem disciplina
                        val grupos = remember(avaliacoesFiltrados) {
                            avaliacoesFiltrados.groupBy { it.disciplina?.id ?: -1L }
                                .toList()
                                .sortedBy { (_, lista) -> (lista.firstOrNull()?.disciplina?.nome ?: "\uFFFF").lowercase() }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(
                                items = grupos,
                                key = { (discId, _) -> discId }
                            ) { (discId, listaDaDisciplina) ->

                                val nomeDisciplina = listaDaDisciplina.firstOrNull()?.disciplina?.nome ?: "[Sem disciplina]"
                                DisciplinaGrupoCard(
                                    nome = nomeDisciplina,
                                    podeAdicionar = discId != -1L, // não mostra botão se não houver disciplina
                                    avaliacoes = listaDaDisciplina.sortedBy { it.descricao ?: "" },
                                    onAddClick = { onAddAvaliacaoParaDisciplina(discId.toString()) },
                                    onAvaliacaoClick = { av -> av.id?.let { onNavigateToManterAvaliacao(it.toString()) } },
                                    onExcluirClick = { av ->
                                        av.id?.let {
                                            viewModel.deleteAvaliacao(it.toString()) { sucesso ->
                                                if (sucesso) {
                                                    Toast.makeText(context, "Avaliação excluída!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

// Composable para o Item Expansível da Lista
@Composable
fun DisciplinaGrupoCard(
    nome: String,
    podeAdicionar: Boolean,
    avaliacoes: List<Avaliacao>,
    onAddClick: () -> Unit,
    onAvaliacaoClick: (Avaliacao) -> Unit,
    onExcluirClick: (Avaliacao) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = CardDefaultBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            // Cabeçalho da disciplina
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = nome,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Recolher" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp)
                ) {
                    // Lista de avaliações da disciplina
                    if (avaliacoes.isEmpty()) {
                        Text(
                            "Sem avaliações nesta disciplina.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )
                    } else {
                        avaliacoes.forEach { av ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAvaliacaoClick(av) }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = av.descricao ?: "[sem descrição]",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    val subtitulo = buildString {
                                        av.tipoAvaliacao?.let { append(it) }
                                        if (av.dataEntrega != null) {
                                            if (isNotEmpty()) append(" • ")
                                            append(av.dataEntrega.toString())
                                        }
                                    }
                                    if (subtitulo.isNotBlank()) {
                                        Text(
                                            text = subtitulo,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                // Excluir
                                IconButton(onClick = { onExcluirClick(av) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Excluir Avaliação",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                                // Editar
                                IconButton(onClick = { onAvaliacaoClick(av) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Editar Avaliação",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Botão Adicionar avaliação (por disciplina)
                    if (podeAdicionar) {
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = onAddClick) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Adicionar avaliação")
                        }
                    }
                }
            }
        }
    }
}

