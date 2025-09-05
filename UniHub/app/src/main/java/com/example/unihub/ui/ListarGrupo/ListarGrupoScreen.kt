package com.example.unihub.ui.ListarGrupo

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.clickable
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
import com.example.unihub.data.model.Grupo
//import com.example.unihub.ui.ListarGrupo.Grupo
import com.example.unihub.ui.ListarGrupo.ListarGrupoViewModel
import com.example.unihub.ui.ListarGrupo.ListarGrupoViewModelFactory

val CardDefaultBackgroundColor = Color(0xFFFFC1C1) // Exemplo de cor padrão


@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun ListarGrupoScreen(
    viewModel: ListarGrupoViewModel = viewModel(factory = ListarGrupoViewModelFactory),
    onAddGrupo: () -> Unit,
    onVoltar: () -> Unit,
    onGrupoClick: (grupoId: String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val gruposState by viewModel.grupos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    // Novo: Estado para controlar o diálogo de exclusão
    var showDeleteDialog by remember { mutableStateOf(false) }
    var grupoParaExcluir by remember { mutableStateOf<Grupo?>(null) }

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
                viewModel.loadGrupo()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val gruposFiltrados = if (searchQuery.isBlank()) {
        gruposState
    } else {
        gruposState.filter {
            it.nome.contains(searchQuery, ignoreCase = true) ||
                    //it.membros.any { membro -> membro.nome.contains(searchQuery, ignoreCase = true) } ||
                    it.id.toString().contains(searchQuery, ignoreCase = true)
        }
    }

    // Diálogo de Confirmação de Exclusão
    if (showDeleteDialog && grupoParaExcluir != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                grupoParaExcluir = null
            },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Deseja realmente excluir o grupo \"${grupoParaExcluir?.nome}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        grupoParaExcluir?.let {
                            // Chame a função de exclusão no ViewModel
                            viewModel.deleteGrupo(it.id.toString()) { sucesso ->
                                if (sucesso) {
                                    Toast.makeText(context, "Grupo excluído!", Toast.LENGTH_SHORT).show()
                                    // A lista deve ser recarregada pelo ViewModel após a exclusão
                                } else {
                                    // O ViewModel deve tratar o errorMessage
                                }
                            }
                        }
                        showDeleteDialog = false
                        grupoParaExcluir = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        grupoParaExcluir = null
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
                titulo = "Grupos",
                onVoltar = onVoltar,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddGrupo,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Grupo")
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
                                    Text("Buscar por nome do grupo", color = Color.Gray, fontSize = 16.sp)
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
                    gruposState.isEmpty() && !isLoading && errorMessage == null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            //contentAlignment = Alignment.Center''
                        ) {
                            Text(
                                "Nenhum grupo cadastrado.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    gruposFiltrados.isEmpty() && searchQuery.isNotBlank() && !isLoading && errorMessage == null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhum grupo encontrado para \"$searchQuery\"",
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
                            /*
                            items(gruposFiltrados, key = { it.id }) { grupo ->
                                GrupoItem(
                                    grupo = grupo,
                                    onClick = {
                                        onGrupoClick(grupo.id.toString())
                                    },
                                    onDeleteClick = { // Nova ação para o clique na lixeira
                                        grupoParaExcluir = grupo
                                        showDeleteDialog = true
                                    }
                                )
                            }

                             */


                        }
                    }
                }
            }
        }
    )
}

@Composable
fun GrupoItem(
    grupo: Grupo,
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
                    text = grupo.nome,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (grupo.membros.isEmpty()) {
                    /*Text(
                        text = grupo.membros,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    */

                    /*
                    if (grupo.pendente) {
                        Text(
                            text = "PENDENTE",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    */

                }


            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Excluir Grupo",
                    tint = MaterialTheme.colorScheme.error // Cor de erro para o ícone de exclusão
                )
            }
        }
    }
}