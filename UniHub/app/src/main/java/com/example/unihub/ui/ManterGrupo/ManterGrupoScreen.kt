package com.example.unihub.ui.ManterGrupo

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import com.example.unihub.ui.ManterGrupo.CardDefaultBackgroundColor
import com.example.unihub.ui.ManterGrupo.ManterGrupoViewModel
import com.example.unihub.ui.ManterGrupo.ManterGrupoViewModelFactory

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

    // Efeitos (sucesso, erro, loadGrupo) - Mantidos como estão
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
    // ---

    Scaffold(
        topBar = {
            CabecalhoAlternativo(
                titulo = if (grupoId == null) "Novo Grupo" else "Editar Grupo",
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


            LaunchedEffect(uiState.nome) { nomeState = uiState.nome }


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
                        text = "Dados do Grupo",
                        style = MaterialTheme.typography.titleMedium, // Um título para a seção
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField( // Usar OutlinedTextField para um visual mais definido
                        value = nomeState,
                        onValueChange = { nomeState = it },
                        label = { Text("Nome do Grupo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors( // Cores podem ser ajustadas
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
                    FALTA IMPLEMENTAR  A FUNÇÃO DE ENVIAR EMAIL
                    FALTA IMPLEMENTAR  A FUNÇÃO DE CONVIDAR
                     */
                    ////////////////////////////////
                    ////////////////////////////////

                    onClick = {
                        if (grupoId == null) {
                            viewModel.createGrupo(nomeState)
                        } else {
                            viewModel.updateGrupo(grupoId, nomeState)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors( // Estilo dos botões principais
                        containerColor = Color(0xFFD3D3D3), // Cor
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        if (grupoId == null) "Criar Grupo" else "Atualizar Grupo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

            }
            Spacer(modifier = Modifier.height(8.dp)) // Espaço extra no final antes da borda da tela
        }
    }
}