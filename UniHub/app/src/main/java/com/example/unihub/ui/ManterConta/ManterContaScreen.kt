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
import com.example.unihub.ui.Shared.NotaCampo

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManterContaScreen(
    onVoltar: () -> Unit,
    onNavigateToManterInstituicao: (String, String, String) -> Unit,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF2F2F2))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background (
                    color = Color(0xFFF2F2F2),
                )
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    // .background(Color(0xFFB2DDF3), CircleShape)
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
                        .padding(top = 8.dp)
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
                            val mediaFormatada = viewModel.media.takeIf { it.isNotBlank() }?.let { NotaCampo.formatFieldText(it) } ?: "-"
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

                TextButton(
                    onClick = { /* deletar conta */ },
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 16.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deletar conta", color = Color.Red)
                }

                              }

            Button(
                onClick = {
                    viewModel.salvar()
                },
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

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Preview(showBackground = true)
@Composable
fun ManterContaScreenPreview() {
    val context = LocalContext.current
    ManterContaScreen(
        onVoltar = {},
        onNavigateToManterInstituicao = { _, _, _ -> },
        viewModel = ManterContaViewModel(
            InstituicaoRepository(ApiInstituicaoBackend(), context),
            AuthRepository(),
            context
        )
    )
}
