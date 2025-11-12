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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator // Para o estado de loading
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.components.CampoBusca
import kotlin.text.contains
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import com.example.unihub.data.config.TokenManager
import com.example.unihub.ui.Shared.ZeroInsets



//Cores
val CardDefaultBackgroundColor = Color(0xFFFFC1C1) // Exemplo de cor padrão


@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable

fun ListarContatoScreen(
    viewModel: ListarContatoViewModel = viewModel(factory = ListarContatoViewModelFactory),
    onAddContato: () -> Unit,
    onVoltar: () -> Unit,
    onContatoClick: (contatoId: String) -> Unit,
    onNavigateToGrupos: () -> Unit
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val context = LocalContext.current
    val contatosState by viewModel.contatos.collectAsState()
    val convitesRecebidosState by viewModel.convitesRecebidos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var contatoParaExcluir by remember { mutableStateOf<ContatoResumoUi?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Contatos", "Convites Enviados", "Convites Recebidos")

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

    val usuarioEmail = TokenManager.emailUsuario?.trim().orEmpty()
    val usuarioEmailLower = remember(usuarioEmail) { usuarioEmail.lowercase() }

    val contatosAtivos = remember(contatosState) { contatosState.filter { !it.pendente } }
    val contatosPendentes = remember(contatosState) { contatosState.filter { it.pendente } }
    val convitesRecebidos = remember(convitesRecebidosState) { convitesRecebidosState }
    val convitesEnviados = remember(contatosPendentes, usuarioEmailLower) {
        if (usuarioEmailLower.isBlank()) {
            contatosPendentes
        } else {
            contatosPendentes.filterNot { it.email.lowercase() == usuarioEmailLower }
        }
    }

    val contatosDaAba = when (selectedTabIndex) {
        0 -> contatosAtivos
        1 -> convitesEnviados
        else -> convitesRecebidos

    }

    val contatosFiltrados = if (searchQuery.isBlank()) {
        contatosDaAba
    } else {
        contatosDaAba.filter {
            it.nome.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
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
                            viewModel.deleteContato(it.registroId) { sucesso ->
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
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.navigationBarsPadding().padding(vertical = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Contato")
            }
        },
        contentWindowInsets = ZeroInsets,

        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CampoBusca(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "Buscar por nome ou e-mail",
                        modifier = Modifier
                            .weight(2f)
                    )
                    Button(
                        onClick = onNavigateToGrupos,
                        modifier = Modifier
                            .weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CardDefaultBackgroundColor,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("ir para Grupos",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface



                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    isLoading && contatosState.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    contatosDaAba.isEmpty() && !isLoading && errorMessage == null && searchQuery.isBlank() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val mensagem = when (selectedTabIndex) {
                                0 -> "Nenhum contato cadastrado."
                                1 -> "Nenhum convite enviado."
                                else -> if (usuarioEmail.isBlank()) {
                                    "Não foi possível identificar seu e-mail para verificar convites recebidos."
                                } else {
                                    "Nenhum convite recebido."
                                }
                            }
                            Text(
                                mensagem,
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
                            val mensagemBusca = when (selectedTabIndex) {
                                0 -> "Nenhum contato encontrado para \"$searchQuery\""
                                1 -> "Nenhum convite enviado encontrado para \"$searchQuery\""
                                else -> "Nenhum convite recebido encontrado para \"$searchQuery\""
                            }
                            Text(
                                text = mensagemBusca,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    else -> {
                        if (isLoading) { // Mostrar um indicador de progresso menor no topo se atualizando
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                            )
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
                                val isConviteRecebido = selectedTabIndex == 2
                                ContatoItem(
                                    contato = contato,
                                    onClick = {
                                        onContatoClick(contato.id.toString())
                                    },
                                    onDeleteClick = { // Nova ação para o clique na lixeira
                                        contatoParaExcluir = contato
                                        showDeleteDialog = true
                                    },
                                    showDelete = !isConviteRecebido,
                                    onAcceptClick = if (isConviteRecebido) {
                                        {
                                            viewModel.aceitarConvite(contato.registroId) { sucesso ->
                                                if (sucesso) {
                                                    Toast.makeText(
                                                        context,
                                                        "Convite aceito!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    } else {
                                        null
                                    },
                                    onRejectClick = if (isConviteRecebido) {
                                        {
                                            viewModel.rejeitarConvite(contato.registroId) { sucesso ->
                                                if (sucesso) {
                                                    Toast.makeText(
                                                        context,
                                                        "Convite rejeitado.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    } else {
                                        null
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
    onDeleteClick: () -> Unit,
    showDelete: Boolean = true,
    onAcceptClick: (() -> Unit)? = null,
    onRejectClick: (() -> Unit)? = null
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
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
                if (!showDelete && (onAcceptClick != null || onRejectClick != null)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        onAcceptClick?.let { onClick ->
                            TextButton(onClick = onClick) {
                                Text("Aceitar")
                            }
                        }
                        onRejectClick?.let { onClick ->
                            TextButton(onClick = onClick) {
                                Text("Rejeitar", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            if (showDelete) {
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
}

