package com.example.unihub.ui.ManterContato

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // Para rolagem
import androidx.compose.foundation.shape.RoundedCornerShape // Para formas de botões e cards
import androidx.compose.foundation.verticalScroll // Para rolagem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete // Ícone para excluir
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Para cores personalizadas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.components.CabecalhoAlternativo // Seu componente de cabeçalho

// Cores e constantes da ListarContatoScreen que queremos replicar
val CardDefaultBackgroundColor = Color(0xFFFFC1C1) // Cor de fundo do Card
val DeleteButtonErrorColor = Color(0xFFB00020) // Uma cor de erro típica para o botão excluir

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun ManterContatoScreen(
    contatoId: String?,
    viewModel: ManterContatoViewModel = viewModel(factory = ManterContatoViewModelFactory()),
    onVoltar: () -> Unit,
    onExcluirSucessoNavegarParaLista: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Efeitos (sucesso, erro, loadContato) - Mantidos como estão
    LaunchedEffect(uiState.sucesso, uiState.isExclusao) {
        if (uiState.sucesso) {
            if (uiState.isExclusao) {
                Toast.makeText(context, "Contato excluído com sucesso!", Toast.LENGTH_SHORT).show()
                onExcluirSucessoNavegarParaLista()
            } else {
                Toast.makeText(context, "Contato salvo com sucesso!", Toast.LENGTH_SHORT).show()
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

    LaunchedEffect(contatoId) {
        if (contatoId != null) {
            viewModel.loadContato(contatoId)
        }
    }
    // ---

    Scaffold(
        topBar = {
            CabecalhoAlternativo(
                titulo = if (contatoId == null) "Novo Contato" else "Editar Contato",
                onVoltar = onVoltar
                // Se o CabecalhoAlternativo tiver opções de cor, configure-as aqui
                // backgroundColor = MaterialTheme.colorScheme.primary, // Exemplo
                // contentColor = MaterialTheme.colorScheme.onPrimary  // Exemplo
            )
        },
        // Não teremos um FAB aqui, mas sim botões de ação no final do formulário
    ) { paddingValues ->

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmar Exclusão") },
                text = { Text("Tem certeza de que deseja excluir este contato?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            contatoId?.let { viewModel.deleteContato(it) }
                            showDeleteDialog = false
                        }
                    ) { Text("Excluir", color = DeleteButtonErrorColor) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Padding do Scaffold
                .padding(horizontal = 16.dp, vertical = 16.dp) // Padding geral do conteúdo
                .verticalScroll(rememberScrollState()), // Adicionar rolagem
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Espaço entre os elementos principais
        ) {
            var nomeState by remember(uiState.nome) { mutableStateOf(uiState.nome) }
            var emailState by remember(uiState.email) { mutableStateOf(uiState.email) }

            LaunchedEffect(uiState.nome) { nomeState = uiState.nome }
            LaunchedEffect(uiState.email) { emailState = uiState.email }

            // Card para os campos de entrada
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp), // Similar ao CardItem
                colors = CardDefaults.cardColors(
                    containerColor = CardDefaultBackgroundColor // Mesma cor do CardItem
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Espaço entre os campos
                ) {
                    Text(
                        text = "Dados do Contato",
                        style = MaterialTheme.typography.titleMedium, // Um título para a seção
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField( // Usar OutlinedTextField para um visual mais definido
                        value = nomeState,
                        onValueChange = { nomeState = it },
                        label = { Text("Nome do Contato") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors( // Cores podem ser ajustadas
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    )

                    OutlinedTextField(
                        value = emailState,
                        onValueChange = { emailState = it },
                        label = { Text("E-mail do Contato") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // Indicador de Carregamento
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))
            }

            // Botões de Ação
            // Usar um Column para os botões se precisar de mais de um com espaçamento
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp) // Espaço entre os botões
            ) {
                // Botão Salvar/Atualizar
                Button(
                    
                    ////////////////////////////////
                    ////////////////////////////////
                    /*
                    FALTA IMPLEMENTAR  AS FUNÇÃO DE ENVIAR EMAIL
                    FALTA IMPLEMENTAR  AS FUNÇÃO DE CONVIDAR
                     */
                    ////////////////////////////////
                    ////////////////////////////////

                    onClick = {
                        if (contatoId == null) {
                            viewModel.createContato(nomeState, emailState)
                        } else {
                            viewModel.updateContato(contatoId, nomeState, emailState)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors( // Estilo dos botões principais
                        containerColor = Color(0xFFCD9B9B), // Cor
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        if (contatoId == null) "Enviar Solicitação" else "Atualizar Contato",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

            }
            Spacer(modifier = Modifier.height(8.dp)) // Espaço extra no final antes da borda da tela
        }
    }
}

