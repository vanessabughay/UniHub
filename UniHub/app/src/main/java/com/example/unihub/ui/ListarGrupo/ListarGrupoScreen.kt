package com.example.unihub.ui.ListarGrupo

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown // NOVO
import androidx.compose.material.icons.filled.KeyboardArrowUp // NOVO
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.components.CampoBusca
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.model.Contato
import com.example.unihub.data.model.Grupo
import com.example.unihub.data.repository.ContatoResumo
import com.example.unihub.ui.Shared.ZeroInsets


//Cores
val CardDefaultBackgroundColor = Color(0xFFF0F0F0)

enum class GrupoAcao {
    SAIR,
    EXCLUIR
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun ListarGrupoScreen(
    viewModel: ListarGrupoViewModel = viewModel(factory = ListarGrupoViewModelFactory),
    onAddGrupo: () -> Unit,
    onVoltar: () -> Unit,
    onNavigateToManterGrupo: (grupoId: String) -> Unit,
    onNavigateToContatos: () -> Unit
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val context = LocalContext.current
    val gruposState by viewModel.grupos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val contatosDoUsuario by viewModel.contatosDoUsuario.collectAsState()

    var grupoExpandidoId by remember { mutableStateOf<Long?>(null) }
    var showConfirmActionDialog by remember { mutableStateOf(false) }
    var grupoSelecionado by remember { mutableStateOf<Grupo?>(null) }
    var acaoGrupoSelecionada by remember { mutableStateOf<GrupoAcao?>(null) }

    var searchQuery by remember { mutableStateOf("") }

    val usuarioLogadoId = TokenManager.usuarioId

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadGrupo()
                viewModel.loadContatosDoUsuario()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val gruposComIdValido by remember(gruposState) {
        derivedStateOf {
            gruposState.filter { it.id != null }
        }
    }

    val gruposFiltrados by remember(searchQuery, gruposComIdValido) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                gruposComIdValido
            } else {
                gruposComIdValido.filter { grupo ->
                    grupo.nome.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    // Diálogo de confirmação para as ações de sair ou excluir o grupo
    if (showConfirmActionDialog && grupoSelecionado != null && acaoGrupoSelecionada != null) {
        val grupoAtual = grupoSelecionado!!
        val acaoAtual = acaoGrupoSelecionada!!
        val tituloDialogo: String
        val mensagemDialogo: String
        val rotuloConfirmacao: String
        val mensagemSucesso: String
        when (acaoAtual) {
            GrupoAcao.EXCLUIR -> {
                tituloDialogo = "Confirmar Exclusão"
                mensagemDialogo = "Deseja realmente excluir o grupo \"${grupoAtual.nome}\"?"
                rotuloConfirmacao = "Excluir"
                mensagemSucesso = "Grupo excluído!"
            }
            GrupoAcao.SAIR -> {
                tituloDialogo = "Confirmar Saída"
                mensagemDialogo = "Deseja realmente sair do grupo \"${grupoAtual.nome}\"?"
                rotuloConfirmacao = "Sair"
                mensagemSucesso = "Você saiu do grupo!"
            }
        }
        AlertDialog(
            onDismissRequest = {
                showConfirmActionDialog = false
                grupoSelecionado = null
                acaoGrupoSelecionada = null
            },
            title = { Text(tituloDialogo) },
            text = { Text(mensagemDialogo) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val grupoId = grupoAtual.id?.toString()
                        if (grupoId != null) {
                            when (acaoAtual) {
                                GrupoAcao.EXCLUIR -> {
                                    viewModel.deleteGrupo(grupoId) { sucesso ->
                                        if (sucesso) {
                                            Toast.makeText(context, mensagemSucesso, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }

                                GrupoAcao.SAIR -> {
                                    viewModel.leaveGrupo(grupoId) { sucesso ->
                                        if (sucesso) {
                                            Toast.makeText(context, mensagemSucesso, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                        showConfirmActionDialog = false
                        grupoSelecionado = null
                        acaoGrupoSelecionada = null
                    }
                ) { Text(rotuloConfirmacao, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmActionDialog = false
                    grupoSelecionado = null
                    acaoGrupoSelecionada = null
                }) { Text("Cancelar") }
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.navigationBarsPadding().padding(vertical = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Grupo")
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
                        placeholder = "Buscar por nome do grupo",
                        modifier = Modifier
                            .weight(2f)
                    )
                    Button(
                        onClick = onNavigateToContatos,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text("ir para Contatos", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
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
                    gruposComIdValido.isEmpty() && !isLoading && errorMessage == null && searchQuery.isBlank() -> {
                        Box( modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp) ) {
                            Text("Nenhum grupo cadastrado.", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    // A condição para "Nenhum grupo encontrado para a busca" usa gruposFiltrados
                    gruposFiltrados.isEmpty() && searchQuery.isNotBlank() && !isLoading && errorMessage == null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nenhum grupo encontrado para \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    else -> {
                        if (isLoading && gruposComIdValido.isNotEmpty()) { // Mostrar LinearProgressIndicator apenas se já houver itens
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
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(
                                items = gruposFiltrados,
                                key = { grupo -> grupo.id!! }
                            ) { grupo ->
                                GrupoItemExpansivel( // Usando o novo item expansível
                                    grupo = grupo,
                                    isExpanded = grupoExpandidoId == grupo.id,
                                    onHeaderClick = {
                                        grupoExpandidoId = if (grupoExpandidoId == grupo.id) {
                                            null
                                        } else {
                                            grupo.id
                                        }
                                    },
                                    onEditarClick = {
                                        grupoExpandidoId = null // Recolhe ao editar
                                        // grupo.id é não nulo aqui
                                        onNavigateToManterGrupo(grupo.id!!.toString())
                                    },
                                    onAcaoClick = { acao ->
                                        grupoExpandidoId = null
                                        grupoSelecionado = grupo
                                        acaoGrupoSelecionada = acao
                                        showConfirmActionDialog = true
                                    },
                                    usuarioLogadoId = usuarioLogadoId,
                                    contatosDoUsuario = contatosDoUsuario
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
fun GrupoItemExpansivel(
    grupo: Grupo,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    onEditarClick: () -> Unit,
    onAcaoClick: (GrupoAcao) -> Unit,
    usuarioLogadoId: Long?,
    contatosDoUsuario: List<ContatoResumo>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDefaultBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onHeaderClick)
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            // Cabeçalho (Sempre Visível)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = "Ícone de Grupo",
                        modifier = Modifier.padding(end = 12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = grupo.nome,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Recolher" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Conteúdo Expansível
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        "Integrantes:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    ) {

                        val membrosOrdenados = grupo.membros
                            .sortedBy { contato -> contato.id ?: Long.MAX_VALUE }

                        val membrosParaExibir = if (membrosOrdenados.isNotEmpty()) {
                            membrosOrdenados
                        } else {
                            val ownerId = grupo.ownerId
                            val nomeAdministrador = TokenManager.nomeUsuario?.trim()?.takeIf { it.isNotEmpty() }
                                ?: TokenManager.emailUsuario?.trim()?.takeIf { it.isNotEmpty() }
                                ?: ownerId?.let { "Usuário #$it" }
                            if (ownerId != null && nomeAdministrador != null) {
                                listOf(
                                    Contato(
                                        id = grupo.adminContatoId ?: ownerId,
                                        nome = nomeAdministrador,
                                        email = TokenManager.emailUsuario,
                                        pendente = false,
                                        idContato = ownerId,
                                        ownerId = ownerId
                                    )
                                )
                            } else {
                                emptyList()
                            }
                        }

                        var acaoGrupo = GrupoAcao.SAIR
                        var iconeAcao = Icons.Filled.Logout
                        var descricaoAcao = "Sair do Grupo"
                        val isUsuarioLogadoAdmin = usuarioLogadoId != null && grupo.ownerId == usuarioLogadoId

                        if (membrosParaExibir.isEmpty()) {
                            Text(
                                "Nenhum integrante neste grupo.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            val contatosDisponiveis = if (usuarioLogadoId != null) {
                                contatosDoUsuario.filter { it.ownerId == usuarioLogadoId }
                            } else {
                                emptyList()
                            }

                            val contatosPorEmail = contatosDisponiveis
                                .mapNotNull { contatoResumo ->
                                    contatoResumo.email.trim().takeIf { it.isNotEmpty() }
                                        ?.lowercase()
                                        ?.let { it to contatoResumo }
                                }
                                .toMap()

                            val contatosPorIdContato = contatosDisponiveis.associateBy { it.id }
                            val membrosExibidos = linkedSetOf<String>()
                            var usuarioLogadoEstaNoGrupo = false
                            var totalMembrosExibidos = 0

                            val nomeUsuarioLogado = TokenManager.nomeUsuario
                                ?.trim()
                                ?.takeIf { it.isNotEmpty() }
                            val emailUsuarioLogadoNormalizado = TokenManager.emailUsuario
                                ?.trim()
                                ?.takeIf { it.isNotEmpty() }
                                ?.lowercase()


                            membrosParaExibir.forEachIndexed { index, contato ->
                                val emailDisponivel =
                                    contato.email?.trim()?.takeIf { it.isNotEmpty() }
                                val emailNormalizado = emailDisponivel?.lowercase()

                                val chaveUnica = contato.idContato?.let { "idContato:$it" }
                                    ?: emailNormalizado?.let { "email:$it" }
                                    ?: contato.id?.let { "id:$it" }
                                    ?: contato.nome?.trim()?.takeIf { it.isNotEmpty() }?.lowercase()
                                        ?.let { "nome:$it" }
                                    ?: "indice:$index"

                                if (!membrosExibidos.add(chaveUnica)) {
                                    return@forEachIndexed
                                }

                                totalMembrosExibidos += 1

                                val contatoResumoAssociado = contato.idContato?.let { contatoId ->
                                    contatosPorIdContato[contatoId]
                                } ?: emailNormalizado?.let { contatosPorEmail[it] }

                                val nomeContato = contatoResumoAssociado?.nome
                                    ?.trim()
                                    ?.takeIf { it.isNotEmpty() }

                                val emailContato = contatoResumoAssociado?.email
                                    ?.trim()
                                    ?.takeIf { it.isNotEmpty() }
                                    ?: emailDisponivel

                                val isUsuarioLogado = (
                                        usuarioLogadoId != null &&
                                                contato.idContato != null &&
                                                contato.idContato == usuarioLogadoId
                                        ) || (
                                        emailUsuarioLogadoNormalizado != null &&
                                                emailNormalizado == emailUsuarioLogadoNormalizado
                                        )

                                if (isUsuarioLogado) {
                                    usuarioLogadoEstaNoGrupo = true
                                }

                                val textoBase = when {
                                    isUsuarioLogado && nomeUsuarioLogado != null -> nomeUsuarioLogado
                                    nomeContato != null -> nomeContato
                                    emailContato != null -> emailContato
                                    else -> contato.nome?.trim()?.takeIf { it.isNotEmpty() }
                                        ?: "Sem identificação"
                                }

                                val isAdministrador = grupo.ownerId != null &&
                                        contato.idContato != null &&
                                        contato.idContato == grupo.ownerId

                                val rotuloAdministrador = if (isAdministrador) {
                                    " (Administrador do Grupo)"
                                } else {
                                    ""
                                }



                                Text(
                                    text = "- $textoBase$rotuloAdministrador",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                            val podeExcluirGrupo =
                                isUsuarioLogadoAdmin && usuarioLogadoEstaNoGrupo && totalMembrosExibidos <= 1
                            acaoGrupo = if (podeExcluirGrupo) GrupoAcao.EXCLUIR else GrupoAcao.SAIR
                            if (acaoGrupo == GrupoAcao.EXCLUIR) {
                                iconeAcao = Icons.Filled.Delete
                                descricaoAcao = "Excluir Grupo"
                            }
                        }



                        Spacer(modifier = Modifier.height(24.dp)) // Aumentar o espaço antes dos botões

                        // Botões de Ação (Editar e sair/Excluir)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween // Para separar os ícones
                        ) {
                            // Botão Sair (esquerda)
                            IconButton(onClick = { onAcaoClick(acaoGrupo) }) {
                                Icon(
                                    imageVector = iconeAcao,
                                    contentDescription = descricaoAcao,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }

                            // Botão Editar (direita)
                            if (isUsuarioLogadoAdmin) {
                                IconButton(onClick = onEditarClick) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Editar Grupo",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }


                        }
                    }
                }
            }
        }
    }
}

