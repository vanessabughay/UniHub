package com.example.unihub.ui.ManterConta

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.data.model.Instituicao

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManterContaScreen(
    onVoltar: () -> Unit,
    viewModel: ManterContaViewModel = viewModel(factory = ManterContaViewModelFactory())
) {
    val sugestoes by remember { derivedStateOf { viewModel.sugestoes } }
    val mostrarCadastrar by remember { derivedStateOf { viewModel.mostrarCadastrar } }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CabecalhoAlternativo(titulo = "Perfil", onVoltar = onVoltar)

            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                modifier = Modifier
                    .size(96.dp)
                    .padding(top = 32.dp)
            )

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
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it },
                label = { Text("E-mail") },
                trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            OutlinedTextField(
                value = viewModel.senha,
                onValueChange = { viewModel.senha = it },
                label = { Text("Senha") },
                visualTransformation = PasswordVisualTransformation(),
                trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Text(
                text = "Instituição",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFB2DDF3))
            ) {
                Box(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = viewModel.nomeInstituicao,
                                onValueChange = {
                                    viewModel.onNomeInstituicaoChange(it)
                                    expanded = true
                                },
                                label = { Text("Instituição") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            if (sugestoes.isNotEmpty()) {
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    sugestoes.forEach { inst: Instituicao ->
                                        DropdownMenuItem(
                                            text = { Text(inst.nome) },
                                            onClick = {
                                                viewModel.onInstituicaoSelecionada(inst)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = viewModel.media,
                            onValueChange = { viewModel.media = it },
                            label = { Text("Média aprovação") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                        OutlinedTextField(
                            value = viewModel.frequencia,
                            onValueChange = { viewModel.frequencia = it },
                            label = { Text("Frequência mínima (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )

                        if (mostrarCadastrar) {
                            Text(
                                text = "Instituição não cadastrada",
                                color = Color.Red,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            TextButton(onClick = { /* ação cadastrar */ }) {
                                Text("Cadastrar nova instituição")
                            }
                        }
                    }

                    IconButton(
                        onClick = { /* editar instituição */ },
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

            Button(
                onClick = { viewModel.salvar() },
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