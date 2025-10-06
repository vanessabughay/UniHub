package com.example.unihub.ui.ListarContato

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.clickable // Usar clickable simples
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator // Para o estado de loading
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.components.SearchBox
import kotlin.text.contains

val CardDefaultBackgroundColor = Color(0xFFFFC1C1) // Exemplo de cor padrão


@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable

fun ListarContatoScreen(
    viewModel: ListarContatoViewModel = viewModel(factory = ListarContatoViewModelFactory),
    onAddContato: () -> Unit,
    onVoltar: () -> Unit,
    onContatoClick: (contatoId: String) -> Unit
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val context = LocalContext.current
    val contatosState by viewModel.contatos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var contatoParaExcluir by remember { mutableStateOf<ContatoResumoUi?>(null) }

    var searchQuery by remember { mutableStateOf("") }

    // Efeito para exibir Toast de erro
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }


    // Efeito para recarregar os dados quando a tela se torna ativa (ON_RESUME)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadContatos()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val contatosFiltrados = if (searchQuery.isBlank()) {
        contatosState
    } else {
        contatosState.filter {
            it.nome.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true) ||
                    it.id.toString().contains(searchQuery, ignoreCase = true)
        }
    }

    // Diálogo de Confirmação de Exclusão
    if (showDeleteDialog && contatoParaExcluir != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                contatoParaExcluir = null
            },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Deseja realmente excluir o contato \"${contatoParaExcluir?.nome}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        contatoParaExcluir?.let {
                            // Chame a função de exclusão no ViewModel
                            viewModel.deleteContato(it.id.toString()) { sucesso ->
                                if (sucesso) {
                                    Toast.makeText(context, "Contato excluído!", Toast.LENGTH_SHORT).show()
                                    // A lista deve ser recarregada pelo ViewModel após a exclusão
                                } else {
                                    // O ViewModel deve tratar o errorMessage
                                }
                            }
                        }
                        showDeleteDialog = false
                        contatoParaExcluir = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        contatoParaExcluir = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CabecalhoAlternativo(
                titulo = "Contatos",
                onVoltar = onVoltar,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddContato,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Contato")
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
                                    Text("Buscar por nome, e-mail ou id", color = Color.Gray, fontSize = 16.sp)
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
                    contatosState.isEmpty() && !isLoading && errorMessage == null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nenhum contato cadastrado.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    contatosFiltrados.isEmpty() && searchQuery.isNotBlank() && !isLoading && errorMessage == null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhum contato encontrado para \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    else -> {
                        if (isLoading) { // Mostrar um indicador de progresso menor no topo se atualizando
                            LinearProgressIndicator(modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp))
                        }
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 16.dp) // Adicionado para espaçamento no final
                        ) {
                            items(contatosFiltrados, key = { it.id }) { contato ->
                                ContatoItem(
                                    contato = contato,
                                    onClick = {
                                        onContatoClick(contato.id.toString())
                                    },
                                    onDeleteClick = { // Nova ação para o clique na lixeira
                                        contatoParaExcluir = contato
                                        showDeleteDialog = true
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


@Composable
fun ContatoItem(
    contato: ContatoResumoUi,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardDefaultBackgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
            ) {
                Text(
                    text = contato.nome,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (contato.email.isNotBlank()) {
                    Text(
                        text = contato.email,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (contato.pendente) {
                        Text(
                            text = "PENDENTE",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Excluir Contato",
                    tint = MaterialTheme.colorScheme.error // Cor de erro para o ícone de exclusão
                )
            }
        }
    }
}

