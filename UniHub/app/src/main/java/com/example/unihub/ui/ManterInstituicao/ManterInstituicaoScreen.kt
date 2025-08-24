package com.example.unihub.ui.ManterInstituicao

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.data.model.Instituicao
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.ui.text.input.KeyboardType

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManterInstituicaoScreen(
    onVoltar: () -> Unit,
    nome: String? = null,
    media: String? = null,
    frequencia: String? = null,
    viewModel: ManterInstituicaoViewModel = viewModel(factory = ManterInstituicaoViewModelFactory())
) {
    val sugestoes by remember { derivedStateOf { viewModel.sugestoes } }
    val mostrarCadastrar by remember { derivedStateOf { viewModel.mostrarCadastrar } }

    LaunchedEffect(nome, media, frequencia) {
        if (!nome.isNullOrBlank()) viewModel.nomeInstituicao = nome
        if (!media.isNullOrBlank()) viewModel.media = media
        if (!frequencia.isNullOrBlank()) viewModel.frequencia = frequencia
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CabecalhoAlternativo(titulo = "Instituição", onVoltar = onVoltar)

            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                modifier = Modifier
                    .size(96.dp)
                    .padding(top = 32.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFB2DDF3))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                    }
                }
            }

            Button(
                onClick = { viewModel.salvar(onVoltar) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5AB9D6)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text("Salvar", color = Color.White)
            }
        }
    }
}