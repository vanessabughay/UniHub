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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.data.model.Anotacao
import com.example.unihub.data.repository.AnotacoesRepository
import com.example.unihub.ui.Anotacoes.AnotacoesViewModel
import androidx.compose.runtime.remember

// Paleta semelhante ao mock
private val Beige = Color(0xFFF5E9DB)
private val BeigeContainer = Color(0xFFF0E0CB)
private val OnBeige = Color(0xFF2E2A25)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnotacoesView(
    disciplinaId: Long,
    onBack: () -> Unit = {},
) {

    // Instancia o repositório e o ViewModel
    val repository = remember { AnotacoesRepository() }
    val vm: AnotacoesViewModel = viewModel(
        factory = AnotacoesViewModelFactory(disciplinaId, repository)
    )

    // Observa o estado das anotações
    val anotacoes by vm.anotacoes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas anotações") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(anotacoes, key = { it.id }) { a ->
                AnotacaoCard(
                    anotacao = a,
                    onToggle = { vm.alternarExpandida(a.id) },
                    onDelete = { vm.excluir(a.id) },
                    onDraftTitleChange = { vm.alterarRascunhoTitulo(a.id, it) },
                    onDraftContentChange = { vm.alterarRascunhoConteudo(a.id, it) },
                    onCancel = { vm.cancelar(a.id) },
                    onSave = { vm.salvar(a.id) }
                )
            }
            item { Spacer(Modifier.height(72.dp)) }
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

            // Cabeçalho do card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // Placeholder do ícone (caixinha bege)
                /*Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(BeigeContainer),
                    contentAlignment = Alignment.Center
                ) {}*/

                Spacer(Modifier.width(12.dp))

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

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, tint = Color(0xC3000000), contentDescription = "Excluir")
                }
            }

            // Área expandida de edição
            AnimatedVisibility(visible = anotacao.expandida) {
                Column(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BeigeContainer)
                        .padding(12.dp)
                ) {
                    OutlinedTextField(
                        value = anotacao.rascunhoTitulo,
                        onValueChange = onDraftTitleChange,
                        label = { Text("Nome da anotação") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = anotacao.rascunhoConteudo,
                        onValueChange = onDraftContentChange,
                        label = { Text("Escreva sua anotação...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Divider(Modifier.padding(vertical = 12.dp), color = Beige)

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
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E2A25))
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                focus.clearFocus()
                                onSave()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E2A25))
                        ) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }
}
