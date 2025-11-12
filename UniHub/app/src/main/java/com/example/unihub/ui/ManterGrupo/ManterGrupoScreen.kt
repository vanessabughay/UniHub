package com.example.unihub.ui.ManterGrupo

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.components.CampoBuscaJanela
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle // Ícone para adicionar
import androidx.compose.material.icons.filled.Delete // Ícone para remover
import androidx.compose.material.icons.filled.Person // Ícone para membro
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.unihub.ui.ListarContato.ContatoResumoUi
import com.example.unihub.data.config.TokenManager
import com.example.unihub.ui.Shared.ZeroInsets


val CardDefaultBackgroundColor = Color(0xFFF0F0F0) // Cor de fundo do Card
val DeleteButtonErrorColor = Color(0xFFB00020) // Uma cor de erro típica para o botão excluir

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun ManterGrupoScreen(
    grupoId: String?,
    viewModel: ManterGrupoViewModel = viewModel(factory = ManterGrupoViewModelFactory()),
    onVoltar: () -> Unit,
    onExcluirSucessoNavegarParaLista: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Estados para controlar a visibilidade dos diálogos de seleção de membros
    var showAddMembrosDialog by remember { mutableStateOf(false) }
    var showRemoveMembrosDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val usuarioIdLogado = TokenManager.usuarioId
    val adminEmailPadrao = TokenManager.emailUsuario?.trim()?.lowercase()
    val adminContato = uiState.membrosDoGrupo.firstOrNull { membro ->
        val emailNormalizado = membro.email.trim().takeIf { it.isNotEmpty() }?.lowercase()
        when {
            uiState.adminContatoId != null && membro.id == uiState.adminContatoId -> true
            adminEmailPadrao != null && emailNormalizado == adminEmailPadrao -> true
            else -> false
        }
    }
    val adminEmailNormalizado = adminContato?.email?.trim()?.takeIf { it.isNotEmpty() }?.lowercase()
        ?: adminEmailPadrao
    val isUsuarioAdministrador = remember(
        grupoId,
        uiState.ownerId,
        usuarioIdLogado,
        uiState.adminContatoId,
        adminEmailNormalizado,
        uiState.membrosDoGrupo
    ) {
        if (grupoId == null) {
            true
        } else {
            val ownerMatches = uiState.ownerId != null && usuarioIdLogado != null && uiState.ownerId == usuarioIdLogado
            val emailMatches = uiState.adminContatoId != null && adminEmailNormalizado != null &&
                    uiState.membrosDoGrupo.any { membro ->
                        val emailNormalizado = membro.email.trim().takeIf { it.isNotEmpty() }?.lowercase()
                        membro.id == uiState.adminContatoId && emailNormalizado == adminEmailNormalizado
                    }
            ownerMatches || emailMatches
        }
    }
    val membrosRemoviveis = uiState.membrosDoGrupo.filterNot { membro ->
        val emailNormalizado = membro.email.trim().takeIf { it.isNotEmpty() }?.lowercase()
        val coincideId = uiState.adminContatoId != null && membro.id == uiState.adminContatoId
        val coincideContato = adminContato?.id == membro.id
        val coincideEmail = adminEmailNormalizado != null && emailNormalizado == adminEmailNormalizado
        coincideId || coincideContato || coincideEmail
    }

    // Efeitos (mantidos como estão)
    LaunchedEffect(uiState.sucesso, uiState.isExclusao) {
        if (uiState.sucesso) {
            if (uiState.isExclusao) {
                Toast.makeText(context, "Grupo excluído com sucesso!", Toast.LENGTH_SHORT).show()
                onExcluirSucessoNavegarParaLista()
            } else {
                Toast.makeText(context, "Grupo salvo com sucesso!", Toast.LENGTH_SHORT).show()
                onVoltar()
            }
            viewModel.onEventoConsumido()
        }
    }

    LaunchedEffect(uiState.erro) {
        uiState.erro?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.onEventoConsumido()
        }
    }

    LaunchedEffect(grupoId) {
        if (grupoId != null) {
            viewModel.loadGrupo(grupoId)
        }
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isSaving = false
        }
    }

    Scaffold(
        topBar = {
            CabecalhoAlternativo(
                titulo = if (grupoId == null) "Novo Grupo" else "Editar Grupo",
                onVoltar = onVoltar
            )
        },
        contentWindowInsets = ZeroInsets
    ) { paddingValues ->

        if (isUsuarioAdministrador && showDeleteDialog) { // Diálogo de exclusão do grupo (mantido)
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmar Exclusão") },
                text = { Text("Tem certeza de que deseja excluir este Grupo?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            grupoId?.let { viewModel.deleteGrupo(it) }
                            showDeleteDialog = false
                        }
                    ) { Text("Excluir", color = DeleteButtonErrorColor) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                }
            )
        }

        // Diálogo para Adicionar Membros
        if (isUsuarioAdministrador && showAddMembrosDialog) {
            SelecaoContatosDialog(
                titulo = "Adicionar Contatos ao Grupo",
                contatosDisponiveis = uiState.todosOsContatosDisponiveis,
                idsContatosJaSelecionados = uiState.membrosDoGrupo.map { it.id }.toSet(), // Para desabilitar/marcar os já membros
                onDismissRequest = { showAddMembrosDialog = false },
                onConfirmarSelecao = { idsSelecionadosParaAdicionar ->
                    idsSelecionadosParaAdicionar.forEach { viewModel.addMembroAoGrupoPeloId(it) }
                    showAddMembrosDialog = false
                },
                isLoading = uiState.isLoadingAllContatos,
                loadingError = uiState.errorLoadingAllContatos
            )
        }

        // Diálogo para Remover Membros
        if (isUsuarioAdministrador && showRemoveMembrosDialog) {
            SelecaoContatosDialog(
                titulo = "Remover Contatos do Grupo",
                // Mostra apenas os membros atuais para remoção
                contatosDisponiveis = membrosRemoviveis,
                idsContatosJaSelecionados = emptySet(), // Não aplicável aqui, pois todos são selecionáveis para remoção
                onDismissRequest = { showRemoveMembrosDialog = false },
                onConfirmarSelecao = { idsSelecionadosParaRemover ->
                    idsSelecionadosParaRemover.forEach { viewModel.removeMembroDoGrupoPeloId(it) }
                    showRemoveMembrosDialog = false
                },
                isForRemoval = true // Flag para diferenciar a lógica de seleção/exibição
            )
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var nomeState by remember(uiState.nome) { mutableStateOf(uiState.nome) }
            LaunchedEffect(uiState.nome) {
                nomeState = uiState.nome
                //viewModel.setNomeGrupo(uiState.nome) // Sincroniza o nome no ViewModel também
            }


            // Card para os DADOS DO GRUPO (mantido)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CardDefaultBackgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Dados do Grupo",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = nomeState,
                        onValueChange = {
                            nomeState = it
                            viewModel.setNomeGrupo(it) // Atualiza o nome no ViewModel
                        },
                        label = { Text("Nome do Grupo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = isUsuarioAdministrador,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // NOVO CARD PARA GERENCIAR MEMBROS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CardDefaultBackgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Membros do Grupo",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Exibição dos membros atuais (opcional)
                    val nomeAdministrador = adminContato?.nome?.takeIf { it.isNotBlank() }
                        ?: TokenManager.nomeUsuario?.trim()?.takeIf { it.isNotEmpty() }
                        ?: adminContato?.email?.takeIf { it.isNotBlank() }
                        ?: TokenManager.emailUsuario?.trim()?.takeIf { it.isNotEmpty() }
                        ?: TokenManager.usuarioId?.let { "Usuário #$it" }
                        ?: "Administrador"

                    val membrosFormatados = buildList {
                        add("- $nomeAdministrador (administrador do Grupo)")
                        membrosRemoviveis.forEach { membro ->
                            val nomeAjustado = membro.nome.takeIf { it.isNotBlank() }
                                ?: membro.email.trim().takeIf { it.isNotEmpty() }
                                ?: "Membro"
                            add("- $nomeAjustado")
                        }
                    }

                    if (membrosFormatados.isNotEmpty()) {
                        membrosFormatados.take(5).forEach { descricao ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = "Membro",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(descricao, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        if (membrosFormatados.size > 5) {
                            Text(
                                "... e mais ${membrosFormatados.size - 5}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    } else {
                        Text("Nenhum membro adicionado.", style = MaterialTheme.typography.bodyMedium)
                    }


                    if (isUsuarioAdministrador) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showAddMembrosDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Filled.AddCircle, contentDescription = "Adicionar", modifier = Modifier.padding(end = 4.dp))
                                Text("Adicionar ao Grupo")
                            }
                            Button(
                                onClick = { showRemoveMembrosDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD3D3D3),
                                    contentColor = Color.Black
                                ),
                                enabled = uiState.membrosDoGrupo.isNotEmpty() // Habilita só se houver membros
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remover", modifier = Modifier.padding(end = 4.dp))
                                Text("Remover do Grupo")
                            }
                        }
                    }
                }
            }

            // Indicador de Carregamento (mantido)
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))
            }

            // Botões de Ação (Salvar/Atualizar e Excluir se for edição)
            if (isUsuarioAdministrador) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button( // Botão Salvar/Atualizar Grupo
                        onClick = {
                            if (!isSaving) {
                                isSaving = true // trava o botão e mostra "Salvando..."

                                if (grupoId == null) {
                                    viewModel.createGrupo(uiState.nome)
                                } else {
                                    viewModel.updateGrupo(grupoId, uiState.nome)
                                }

                                // Quando a tela for recarregada, o remember reseta e reativa o botão
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD3D3D3),
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = if (isSaving)
                                "Salvando..." // feedback visual
                            else if (grupoId == null)
                                "Criar Grupo"
                            else
                                "Atualizar Grupo",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (grupoId != null && uiState.podeExcluirGrupo) { // Botão Excluir Grupo (somente em modo de edição e quando permitido)
                        Button(
                            onClick = { showDeleteDialog = true },
                            enabled = !isSaving, // desativa enquanto salva
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeleteButtonErrorColor.copy(alpha = 0.1f),
                                contentColor = DeleteButtonErrorColor
                            )
                        ) {
                            Text("Excluir Grupo", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


@Composable
fun SelecaoContatosDialog(
    titulo: String,
    contatosDisponiveis: List<ContatoResumoUi>,
    idsContatosJaSelecionados: Set<Long> = emptySet(), // Para desabilitar/indicar os já membros no modo de adição
    onDismissRequest: () -> Unit,
    onConfirmarSelecao: (Set<Long>) -> Unit,
    isLoading: Boolean = false,
    loadingError: String? = null,
    isForRemoval: Boolean = false // Se true, todos são selecionáveis para remover (não usa idsContatosJaSelecionados para desabilitar)
) {
    var idsTemporariamenteSelecionados by remember { mutableStateOf(emptySet<Long>()) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(titulo) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isLoading) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (loadingError != null) {
                    Text("Erro: $loadingError", color = MaterialTheme.colorScheme.error)
                } else if (contatosDisponiveis.isEmpty()) {
                    Text(if (isForRemoval) "Nenhum membro para remover." else "Nenhum contato disponível.")
                } else {
                    var termoBusca by remember(contatosDisponiveis) { mutableStateOf("") }
                    val contatosFiltrados = remember(termoBusca, contatosDisponiveis) {
                        if (termoBusca.isBlank()) {
                            contatosDisponiveis
                        } else {
                            contatosDisponiveis.filter { contato ->
                                contato.nome.contains(termoBusca, ignoreCase = true)
                            }
                        }
                    }

                    CampoBuscaJanela(
                        value = termoBusca,
                        onValueChange = { termoBusca = it },
                        placeholder = "Buscar contatos",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (contatosFiltrados.isEmpty()) {
                        Text(
                            text = if (isForRemoval) "Nenhum membro encontrado." else "Nenhum contato encontrado.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Surface( // Usar Surface para dar um limite de altura à LazyColumn dentro do AlertDialog
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp) // Limita a altura
                        ) {
                            LazyColumn {
                                items(contatosFiltrados, key = { it.id }) { contato ->
                                    val isChecked = idsTemporariamenteSelecionados.contains(contato.id)
                                    // No modo de adição, desabilita se já for membro (a menos que seja o próprio membro sendo 're-adicionado')
                                    val isEnabled = isForRemoval || !idsContatosJaSelecionados.contains(contato.id)

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = isEnabled) {
                                                if (isEnabled) {
                                                    idsTemporariamenteSelecionados = if (isChecked) {
                                                        idsTemporariamenteSelecionados - contato.id
                                                    } else {
                                                        idsTemporariamenteSelecionados + contato.id
                                                    }
                                                }
                                            }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = null, // Controlado pelo Row clickable
                                            enabled = isEnabled
                                        )
                                        Text(
                                            text = contato.nome + if (idsContatosJaSelecionados.contains(contato.id) && !isForRemoval) " (Já é membro)" else "",
                                            modifier = Modifier.padding(start = 8.dp),
                                            color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmarSelecao(idsTemporariamenteSelecionados)
                },
                enabled = !isLoading && loadingError == null && contatosDisponiveis.isNotEmpty()
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}