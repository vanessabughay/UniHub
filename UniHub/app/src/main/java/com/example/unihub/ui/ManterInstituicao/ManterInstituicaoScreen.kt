package com.example.unihub.ui.ManterInstituicao

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.activity.compose.BackHandler
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

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManterInstituicaoScreen(
    onVoltar: () -> Unit,
    nome: String = "",
    media: String = "",
    frequencia: String = "",
    mensagemObrigatoria: String = "",
    bloquearSaida: Boolean = false,

    viewModel: ManterInstituicaoViewModel = viewModel(factory = ManterInstituicaoViewModelFactory(LocalContext.current))) {

    val context = LocalContext.current

    val mensagemBloqueio = remember(mensagemObrigatoria, bloquearSaida) {
        if (mensagemObrigatoria.isNotBlank()) mensagemObrigatoria
        else "Informe a instituição para continuar"
    }

    BackHandler(enabled = bloquearSaida) {
        Toast.makeText(context, mensagemBloqueio, Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(nome, media, frequencia) {
        if (nome.isNotBlank()) {
            viewModel.nomeInstituicao = nome
            viewModel.onMediaChange(media)
            viewModel.onFrequenciaChange(frequencia)
        }
    }

    LaunchedEffect(mensagemObrigatoria) {
        if (mensagemObrigatoria.isNotBlank()) {
            Toast.makeText(context, mensagemObrigatoria, Toast.LENGTH_LONG).show()
        }
    }

    val sugestoes by remember { derivedStateOf { viewModel.sugestoes } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            CabecalhoAlternativo(
                titulo = "Instituição",
                onVoltar = onVoltar,
                habilitarVoltar = !bloquearSaida
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (mensagemObrigatoria.isNotBlank()) {
                    Surface(
                        color = Color(0xFFFFE9E9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(
                            text = mensagemObrigatoria,
                            color = Color(0xFFB00020),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 16.sp
                        )
                    }
                }

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
                                                "freq:${PesoCampo.formatListValue(inst.frequenciaMinima.toDouble())})"
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

                Row(
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

                val podeSalvar = viewModel.isFormularioValido()

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
                        .padding(vertical = 16.dp),
                    enabled = podeSalvar
                ) {
                    Text(
                        "Salvar",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                viewModel.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color(0xFFB00020),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    )
                }
            }
        }


    }
    }

