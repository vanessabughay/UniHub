package com.example.unihub.ui.ManterInstituicao

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.components.CabecalhoAlternativo
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.ui.tooling.preview.Preview
import com.example.unihub.data.apiBackend.ApiInstituicaoBackend
import com.example.unihub.data.repository.InstituicaoRepository
import com.example.unihub.ui.Shared.NotaCampo
import com.example.unihub.ui.Shared.PesoCampo
import android.widget.Toast
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManterInstituicaoScreen(
    onVoltar: () -> Unit,
    nome: String = "",
    media: String = "",
    frequencia: String = "",
    viewModel: ManterInstituicaoViewModel = viewModel(factory = ManterInstituicaoViewModelFactory(LocalContext.current))) {

    val context = LocalContext.current

    LaunchedEffect(nome, media, frequencia) {
        if (nome.isNotBlank()) {
            viewModel.nomeInstituicao = nome
            viewModel.onMediaChange(media)
            viewModel.onFrequenciaChange(frequencia)
        }
    }

    val sugestoes by remember { derivedStateOf { viewModel.sugestoes } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF2F2F2))
    ) {
        CabecalhoAlternativo(titulo = "Instituição", onVoltar = onVoltar)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                modifier = Modifier
                    .size(96.dp)
                    .padding(top = 32.dp),
                tint = Color(0xFF243C5B)
            )

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded && sugestoes.isNotEmpty(),
                onExpandedChange = { expanded = it && sugestoes.isNotEmpty() },
                modifier = Modifier.padding(top = 24.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.nomeInstituicao,
                    onValueChange = { viewModel.onNomeInstituicaoChange(it) },
                    label = { Text("Nome da Instituição") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xA8C1D5E4),
                        unfocusedContainerColor = Color(0xA8C1D5E4),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color(0x0D000000)
                    ),
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && sugestoes.isNotEmpty()) }
                )

                LaunchedEffect(sugestoes) { expanded = sugestoes.isNotEmpty() }
                DropdownMenu(
                    expanded = expanded && sugestoes.isNotEmpty(),
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.exposedDropdownSize(true)
                ) {
                    sugestoes.forEach { inst ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "${inst.nome} (média:${NotaCampo.formatListValue(inst.mediaAprovacao)} " +
                                            "freq:${PesoCampo.formatListValue(inst.frequenciaMinima.toDouble())}%)"
                                )
                            },
                            onClick = {
                                viewModel.onInstituicaoSelecionada(inst)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = NotaCampo.formatFieldText(viewModel.media),
                    onValueChange = { viewModel.onMediaChange(it) },
                    label = { Text("Média") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xA8C1D5E4),
                        unfocusedContainerColor = Color(0xA8C1D5E4),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color(0x0D000000)
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.frequencia,
                    onValueChange = { viewModel.onFrequenciaChange(it) },
                    label = { Text("Frequência") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xA8C1D5E4),
                        unfocusedContainerColor = Color(0xA8C1D5E4),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color(0x0D000000)
                    ),
                    singleLine = true,
                    suffix = { Text("%") }
                )
            }

            Button(
                onClick = { viewModel.salvar(onVoltar) },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0x0D000000)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5AB9D6)
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    "Salvar",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}

