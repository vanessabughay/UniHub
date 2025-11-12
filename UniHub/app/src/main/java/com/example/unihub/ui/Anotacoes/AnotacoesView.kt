package com.example.unihub.ui.Anotacoes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.data.model.Anotacao
import com.example.unihub.data.repository.AnotacoesRepository
import androidx.compose.runtime.remember
import com.example.unihub.components.CabecalhoAlternativo
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.unihub.components.CampoBusca
import com.example.unihub.ui.Shared.ZeroInsets


private val Beige = Color(0xFFF8F0E7)
private val BeigeContainer = Color(0x2FE1C4A1)
private val OnBeige = Color(0xFF38332E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnotacoesView(
    disciplinaId: Long,
    onVoltar: () -> Unit,
) {

    val repository = remember { AnotacoesRepository() }
    val vm: AnotacoesViewModel = viewModel(
        factory = AnotacoesViewModelFactory(disciplinaId, repository)
    )

    val anotacoes by vm.anotacoes.collectAsState()
    var anotacaoParaExcluir by remember { mutableStateOf<Anotacao?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredAnotacoes = remember(anotacoes, searchQuery) {
        if (searchQuery.isBlank()) {
            anotacoes
        } else {
            anotacoes.filter {
                it.expandida ||
                        it.titulo.contains(searchQuery, ignoreCase = true) ||
                        it.conteudo.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            CabecalhoAlternativo(
                titulo = "Minhas anotações",
                onVoltar = onVoltar
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = vm::novaAnotacao,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFE0BD),
                        contentColor = OnBeige
                    )
                ) {
                    Text("Nova anotação", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        contentWindowInsets = ZeroInsets
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CampoBusca(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Buscar anotações...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp, top = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredAnotacoes, key = { it.id }) { a ->
                    AnotacaoCard(
                        anotacao = a,
                        onToggle = { vm.alternarExpandida(a.id) },
                        onDelete = { anotacaoParaExcluir = a },
                        onDraftTitleChange = { vm.alterarRascunhoTitulo(a.id, it) },
                        onDraftContentChange = { vm.alterarRascunhoConteudo(a.id, it) },
                        onCancel = { vm.cancelar(a.id) },
                        onSave = { vm.salvar(a.id) }
                    )
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }

        anotacaoParaExcluir?.let { selecionada ->
            AlertDialog(
                onDismissRequest = { anotacaoParaExcluir = null },
                confirmButton = {
                    TextButton(onClick = {
                        vm.excluir(selecionada.id)
                        anotacaoParaExcluir = null
                    }) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { anotacaoParaExcluir = null }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("Confirmar exclusão") },
                text = {
                    Text(
                        text = "Tem certeza de que deseja excluir esta anotação?",
                        color = OnBeige
                    )
                }
            )
        }
    }
}

@Composable
private fun AnotacaoCard(
    anotacao: Anotacao,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onDraftTitleChange: (String) -> Unit,
    onDraftContentChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    val focus = LocalFocusManager.current

    Card(
        colors = CardDefaults.cardColors(containerColor = Beige),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {

                Spacer(Modifier.width(12.dp))

                if (anotacao.expandida) {
                    OutlinedTextField(
                        value = anotacao.rascunhoTitulo,
                        onValueChange = onDraftTitleChange,
                        label = { Text("Nome da anotação") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onToggle() }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = anotacao.titulo,
                            color = OnBeige,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, tint = Color(0xC3000000), contentDescription = "Excluir")
                }
            }

            AnimatedVisibility(visible = anotacao.expandida) {
                Column(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BeigeContainer)
                        .padding(12.dp)
                ) {
                    OutlinedTextField(
                        value = anotacao.rascunhoConteudo,
                        onValueChange = onDraftContentChange,
                        label = { Text("Escreva sua anotação...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    HorizontalDivider(
                        Modifier.padding(vertical = 12.dp),
                        DividerDefaults.Thickness,
                        color = Beige
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                focus.clearFocus()
                                onCancel()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(
                                    0xFF2E2A25
                                )
                            )
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                focus.clearFocus()
                                onSave()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFCBB297),
                                contentColor = Color(0xFFFFFFFF)
                            )
                        ) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }
}