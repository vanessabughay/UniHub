package com.example.unihub.ui.ManterConta

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import android.widget.Toast
import com.example.unihub.data.repository.AuthRepository
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.unihub.data.apiBackend.ApiInstituicaoBackend
import com.example.unihub.R
import com.example.unihub.data.repository.InstituicaoRepository
import androidx.compose.ui.platform.LocalContext
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.ui.Shared.NotaCampo
import androidx.compose.foundation.BorderStroke
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManterContaScreen(
    onVoltar: () -> Unit,
    onNavigateToManterInstituicao: (String, String, String) -> Unit,
    onContaExcluida: () -> Unit,
    viewModel: ManterContaViewModel = viewModel(factory = ManterContaViewModelFactory(LocalContext.current))) {

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.carregarInstituicaoUsuario()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.carregarInstituicaoUsuario()
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.errorMessage = null
        }
    }

    LaunchedEffect(viewModel.success) {
        if (viewModel.success) {
            Toast.makeText(context, "alteração salva com sucesso!", Toast.LENGTH_SHORT).show()
            viewModel.success = false
            onVoltar()
        }
    }

    LaunchedEffect(viewModel.contaExcluida) {
        if (viewModel.contaExcluida) {
            viewModel.consumirContaExcluida()
            onContaExcluida()
        }
    }

    if (viewModel.exibindoDialogoExclusao) {
        AlertDialog(
            onDismissRequest = { viewModel.fecharDialogoExclusao() },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
            title = { Text("Excluir conta") },
            text = {
                Text(
                    "Tem certeza de que deseja excluir sua conta? Essa ação não pode ser desfeita.",
                    color = Color(0xFF1F2937)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deletarConta() },
                    enabled = !viewModel.excluindoConta
                ) {
                    if (viewModel.excluindoConta) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Excluir", color = Color.Red)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.fecharDialogoExclusao() },
                    enabled = !viewModel.excluindoConta
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)

    ) {
        CabecalhoAlternativo(titulo = "Perfil", onVoltar = onVoltar)

        Spacer(Modifier.width(30.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                ,
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ph_student),
                    contentDescription = null,
                    tint = Color(0xFF243C5B),
                    modifier = Modifier.size(96.dp)
                )

            }
            Text(
                text = "Informações gerais",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp)
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            OutlinedTextField(
                value = viewModel.nome,
                onValueChange = { viewModel.nome = it },
                label = { Text("Nome") },
                trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xA8C1D5E4),
                    unfocusedContainerColor = Color(0xA8C1D5E4),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it },
                label = { Text("E-mail") },
                trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xA8C1D5E4),
                    unfocusedContainerColor = Color(0xA8C1D5E4),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            var expandirSenha by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .animateContentSize(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
                ) {
                    OutlinedTextField(
                        value = viewModel.senha,
                        onValueChange = { viewModel.senha = it },
                        label = { Text("Senha") },
                        visualTransformation = PasswordVisualTransformation(),
                        trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xA8C1D5E4),
                            unfocusedContainerColor = Color(0xA8C1D5E4),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (it.isFocused) expandirSenha = true }
                    )
                    if (expandirSenha) {
                        OutlinedTextField(
                            value = viewModel.confirmarSenha,
                            onValueChange = { viewModel.confirmarSenha = it },
                            label = { Text("Confirmar senha") },
                            visualTransformation = PasswordVisualTransformation(),
                            trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xA8C1D5E4),
                                unfocusedContainerColor = Color(0xA8C1D5E4),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }
                }
            }

            Text(
                text = "Instituição",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp)
            )

            if (viewModel.nomeInstituicao.isBlank()) {
                OutlinedButton(
                    onClick = { onNavigateToManterInstituicao("", "", "") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0x0D000000))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Cadastrar instituição")
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0x0D000000)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xA8C1D5E4))
                ) {
                    Box(Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = viewModel.nomeInstituicao,
                                fontWeight = FontWeight.Bold
                            )
                            val mediaFormatada = viewModel.media.takeIf { it.isNotBlank() }
                                ?.let { NotaCampo.formatFieldText(it) } ?: "-"
                            Text("Média de aprovação: $mediaFormatada")
                            Text("Frequência mínima: ${viewModel.frequencia}%")
                        }

                        IconButton(
                            onClick = {
                                onNavigateToManterInstituicao(
                                    viewModel.nomeInstituicao,
                                    viewModel.media,
                                    viewModel.frequencia
                                )

                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    }
                }

            }

                TextButton(
                    onClick = { viewModel.abrirDialogoExclusao() },
                    enabled = !viewModel.excluindoConta,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 16.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deletar conta", color = Color.Red)

            }

            Button(
                onClick = {
                    viewModel.salvar()
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0x0D000000)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5AB9D6)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Salvar", color = Color.White)
            }
        }
    }
}
