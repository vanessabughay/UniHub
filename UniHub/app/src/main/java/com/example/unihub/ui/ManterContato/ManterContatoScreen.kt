package com.example.unihub.ui.ManterContato


import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Import para viewModel()

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun ManterContatoScreen( // << NOME DA FUNÇÃO EXATO e DESCOMENTADO
    contatoId: String?,
    // Use a factory que você definiu para o ManterContatoViewModel
    viewModel: ManterContatoViewModel = viewModel(factory = ManterContatoViewModelFactory()),
    onVoltar: () -> Unit,
    onExcluirSucessoNavegarParaLista: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Efeito para lidar com o sucesso da operação (salvar, atualizar, excluir)
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

    // Efeito para lidar com mensagens de erro
    LaunchedEffect(uiState.erro) {
        uiState.erro?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.onEventoConsumido()
        }
    }

    // Carregar dados do contato se estiver editando
    LaunchedEffect(contatoId) {
        if (contatoId != null) {
            viewModel.loadContato(contatoId)
        }
    }

    // Layout da tela
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var nomeState by remember(uiState.nome) { mutableStateOf(uiState.nome) }
        var emailState by remember(uiState.email) { mutableStateOf(uiState.email) }

        LaunchedEffect(uiState.nome, uiState.email) {
            nomeState = uiState.nome
            emailState = uiState.email
        }

        TextField(
            value = nomeState,
            onValueChange = { nomeState = it },
            label = { Text("Nome do Contato") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = emailState,
            onValueChange = { emailState = it },
            label = { Text("E-mail do Contato") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (contatoId == null) {
                        viewModel.createContato(nomeState, emailState)
                    } else {
                        viewModel.updateContato(contatoId, nomeState, emailState)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (contatoId == null) "Criar Contato" else "Atualizar Contato")
            }

            if (contatoId != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.deleteContato(contatoId) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir Contato")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar / Voltar")
        }
    }
}

