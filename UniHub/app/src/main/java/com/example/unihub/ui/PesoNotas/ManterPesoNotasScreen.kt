package com.example.unihub.ui.PesoNotas

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.data.model.Avaliacao

import java.util.Locale

//Cores
private val PesoNotasColor      = Color(0xFFD8ECDF)       // fundo do card de peso das notas
private val PesoNotasBtnColor = Color(0xFFBED0C4)       // fundo do card de peso das notas

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun ManterPesoNotasScreen(
    disciplinaId: String,
    onVoltar: () -> Unit,
    onAddAvaliacaoParaDisciplina: (disciplinaId: String) -> Unit,
    viewModel: ManterPesoNotasViewModel = viewModel(factory = ManterPesoNotasViewModelFactory)
) {
    val ctx = LocalContext.current
    val ui by viewModel.ui.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Recarrega ao voltar da ManterAvaliacao
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.load() // <<< só isso aqui
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }



    LaunchedEffect(disciplinaId) {
        disciplinaId.toLongOrNull()
            ?.let { viewModel.setDisciplinaId(it) }
            ?: Toast.makeText(ctx, "disciplinaId inválido", Toast.LENGTH_LONG).show()
    }

    // Dialogs
    var editarNotaDe by remember { mutableStateOf<Avaliacao?>(null) }
    var editarPesoDe by remember { mutableStateOf<Avaliacao?>(null) }
    var campoTemp by remember { mutableStateOf("") }

    if (editarNotaDe != null) {
        AlertDialog(
            onDismissRequest = { editarNotaDe = null },
            title = { Text("Editar nota") },
            text = {
                OutlinedTextField(
                    value = campoTemp,
                    onValueChange = { campoTemp = it },
                    singleLine = true,
                    label = { Text("Nota") },
                    placeholder = { Text("Ex.: 8,5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val valor = campoTemp.replace(',', '.').toDoubleOrNull()
                    val alvo = editarNotaDe!!
                    viewModel.salvarNota(alvo, valor) { ok, err ->
                        if (ok) Toast.makeText(ctx, "Nota salva!", Toast.LENGTH_SHORT).show()
                        else Toast.makeText(ctx, err ?: "Erro ao salvar nota.", Toast.LENGTH_LONG).show()
                    }
                    editarNotaDe = null
                }) { Text("SALVAR") }
            },
            dismissButton = { TextButton(onClick = { editarNotaDe = null }) { Text("CANCELAR") } }
        )
    }

    if (editarPesoDe != null) {
        AlertDialog(
            onDismissRequest = { editarPesoDe = null },
            title = { Text("Editar peso (%)") },
            text = {
                OutlinedTextField(
                    value = campoTemp,
                    onValueChange = { campoTemp = it },
                    singleLine = true,
                    label = { Text("Peso (%)") },
                    placeholder = { Text("Ex.: 20") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val valor = campoTemp.replace(',', '.').toDoubleOrNull()
                    val alvo = editarPesoDe!!
                    viewModel.salvarPeso(alvo, valor) { ok, err ->
                        if (ok) Toast.makeText(ctx, "Peso salvo!", Toast.LENGTH_SHORT).show()
                        else Toast.makeText(ctx, err ?: "Erro ao salvar peso.", Toast.LENGTH_LONG).show()
                    }
                    editarPesoDe = null
                }) { Text("SALVAR") }
            },
            dismissButton = { TextButton(onClick = { editarPesoDe = null }) { Text("CANCELAR") } }
        )
    }

    Scaffold(
        topBar = {
            // tenta extrair o nome da disciplina da lista atual
            val nomeDisciplina = remember(ui.itens) {
                ui.itens.firstOrNull()?.disciplina?.nome.orEmpty()
            }

            CabecalhoAlternativo(
                titulo = buildString {
                    append("Notas da disciplina")
                    if (nomeDisciplina.isNotBlank()) {

                        append(nomeDisciplina)

                    }
                },
                onVoltar = onVoltar
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Cabeçalho de colunas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Título", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row {
                    Text("Nota", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(24.dp))
                    Text("Peso", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (ui.isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            if (ui.erro != null) {
                Text(ui.erro!!, color = MaterialTheme.colorScheme.error)
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(ui.itens, key = { it.id ?: it.hashCode().toLong() }) { av ->
                    AvaliacaoLinha(
                        av = av,
                        onEditNota = {
                            campoTemp = av.nota?.let { n -> formatNumero(n) } ?: ""
                            editarNotaDe = av
                        },
                        onEditPeso = {
                            campoTemp = av.peso?.let { p -> formatNumero(p) } ?: ""
                            editarPesoDe = av
                        }
                    )
                }

                // Cart do “Nota geral”
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PesoNotasColor),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Nota geral", fontWeight = FontWeight.SemiBold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(formatNumero(ui.notaGeral), fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.width(24.dp))
                                    Text("${formatNumero(ui.somaPesosComNota)}%")
                                }
                            }
                            if (ui.faltandoParaAprovacao > 0.0) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "FALTA ${formatNumero(ui.faltandoParaAprovacao)} PARA APROVAÇÃO!",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Botão “Adicionar avaliação”
                item {
                    Button(
                        onClick = { onAddAvaliacaoParaDisciplina(disciplinaId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PesoNotasBtnColor,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Adicionar avaliação") }
                }

                // Alerta se pesos ≠ 100%
                if (!viewModel.pesosFecham100()) {
                    item {
                        Text(
                            "ATENÇÃO! A SOMA DOS PESOS ESTÁ DIFERENTE DE 100%!",
                            color = Color(0xFFD63B2F),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvaliacaoLinha(
    av: Avaliacao,
    onEditNota: () -> Unit,
    onEditPeso: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PesoNotasColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Description, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text(
                av.descricao ?: "[sem descrição]",
                modifier = Modifier.weight(1f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            // NOTA (clicável)
            Text(
                text = av.nota?.let { formatNumero(it) } ?: "-",
                modifier = Modifier
                    .widthIn(min = 40.dp)
                    .clickable { onEditNota() },
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(24.dp))

            // PESO (clicável)
            Text(
                text = (av.peso?.let { "${formatNumero(it)}%" } ?: "-"),
                modifier = Modifier
                    .widthIn(min = 42.dp)
                    .clickable { onEditPeso() },
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.width(6.dp))
            //Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Color.Black.copy(alpha = 0.55f))
        }
    }
}

private fun formatNumero(v: Double): String {
    // 1 casa para nota/peso; usa vírgula no PT-BR
    val s = String.format(Locale.US, "%.1f", v)
    return s.replace('.', ',')
}
