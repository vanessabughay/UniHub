package com.example.unihub.ui.ManterInstituicao

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.data.model.Instituicao
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import androidx.compose.ui.tooling.preview.Preview
import com.example.unihub.data.repository.ApiInstituicaoBackend
import com.example.unihub.data.repository.InstituicaoRepository

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManterInstituicaoScreen(
    onVoltar: () -> Unit,
    nome: String = "",
    media: String = "",
    frequencia: String = "",
    viewModel: ManterInstituicaoViewModel = viewModel(factory = ManterInstituicaoViewModelFactory(LocalContext.current))) {
    LaunchedEffect(nome, media, frequencia) {
        if (nome.isNotBlank()) {
            viewModel.nomeInstituicao = nome
            viewModel.media = media
            viewModel.frequencia = frequencia
        }
    }
    val sugestoes by remember { derivedStateOf { viewModel.sugestoes } }
    val mostrarCadastrar by remember { derivedStateOf { viewModel.mostrarCadastrar } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = Color(0xFFFFFFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(color = Color(0xFFF2F2F2))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
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



            Column(
                modifier = Modifier
                    .padding(top = 39.dp)
                    .padding(bottom = 39.dp)
            ) {
                Text(
                    "Nome",
                    color = Color(0xFF000000),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 2.dp, bottom = 2.dp, start = 17.dp, end = 54.dp)
                )
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = viewModel.nomeInstituicao,
                        onValueChange = {
                            viewModel.onNomeInstituicaoChange(it)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .border(
                                width = 1.dp,
                                color = Color(0x0D000000),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .width(266.dp)
                            .height(44.dp)
                            .background(color = Color(0xA8C1D5E4), shape = RoundedCornerShape(10.dp)),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    LaunchedEffect(sugestoes) { expanded = sugestoes.isNotEmpty() }
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        sugestoes.forEach { inst ->
                            DropdownMenuItem(
                                text = { Text(inst.nome) },
                                onClick = {
                                    viewModel.onInstituicaoSelecionada(inst)
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }
            Row (
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Média aprovação",
                    color = Color(0xFF000000),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 2.dp, bottom = 2.dp, start = 0.dp, end = 26.dp)
                )
                Text(
                    "Frequência mínima",
                    color = Color(0xFF000000),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(vertical = 2.dp, horizontal = 20.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(bottom = 320.dp),

            ) {
                OutlinedTextField(
                    value = viewModel.media,
                    onValueChange = { viewModel.media = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0x0D000000),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .width(128.dp)
                        .height(44.dp)
                        .background(color = Color(0xA8C1D5E4), shape = RoundedCornerShape(10.dp)),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.frequencia,
                    onValueChange = { viewModel.frequencia = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = Color(0x0D000000),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .width(128.dp)
                        .height(44.dp)
                        .background(color = Color(0xA8C1D5E4), shape = RoundedCornerShape(10.dp)),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
            OutlinedButton(
                onClick = { viewModel.salvar(onVoltar) },
                border = BorderStroke(0.dp, Color.Transparent),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .padding(bottom = 13.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = Color(0xA878B4E1), shape = RoundedCornerShape(8.dp))
                    .shadow(elevation = 2.dp, spotColor = Color(0x0D000000))
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 7.dp, horizontal = 31.dp)
                ) {
                    Text(
                        "Salvar",
                        color = Color(0xFF000000),
                        fontSize = 16.sp,

                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(bottom = 4.dp, start = 2.dp, end = 2.dp)
                    .fillMaxWidth()
                    .padding(vertical = 17.dp)
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .width(108.dp)
                        .height(6.dp)
                        .background(color = Color(0xFF000000), shape = RoundedCornerShape(100.dp))
                ) {
                }
            }
        }
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Preview(showBackground = true)
@Composable
fun ManterInstituicaoScreenPreview() {
    val context = LocalContext.current
    ManterInstituicaoScreen(
        onVoltar = {},
        viewModel = ManterInstituicaoViewModel(
            InstituicaoRepository(ApiInstituicaoBackend(), context)
        ),

    )
}
