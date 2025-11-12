package com.example.unihub.ui.PesoNotas

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.outlined.Edit
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
import com.example.unihub.ui.Shared.NotaCampo
import com.example.unihub.ui.Shared.PesoCampo
import com.example.unihub.ui.Shared.ZeroInsets



//Cores
private val PesoNotasColor      = Color(0xFFD8ECDF)       // fundo do card de peso das notas
private val PesoNotasBtnColor = Color(0xFFBED0C4)       // fundo do card de peso das notas

// Larguras/espacamentos para alinhar cabeçalho e linhas
private val ICON_SIZE = 24.dp
private val ICON_GAP = 10.dp
private val CARD_SIDE_PAD = 12.dp
private val GAP_NOTA_PESO = 24.dp

// Larguras fixas das colunas numéricas (ajuste se quiser)
private val COL_NOTA_WIDTH = 48.dp
private val COL_PESO_WIDTH = 60.dp



@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun ManterPesoNotasScreen(
    disciplinaId: String,
    onVoltar: () -> Unit,
    onAddAvaliacaoParaDisciplina: (disciplinaId: String) -> Unit,
    onEditarAvaliacao: (avaliacaoId: String, disciplinaId: String) -> Unit
) {
    val viewModel: ManterPesoNotasViewModel = viewModel(
        factory = ManterPesoNotasViewModelFactory(LocalContext.current)
    )
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
                    value = NotaCampo.formatFieldText(campoTemp),
                    onValueChange = { novo -> campoTemp = NotaCampo.sanitize(novo) },
                    singleLine = true,
                    label = { Text("Nota") },
                    placeholder = { Text("Ex.: 8,5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val valor = NotaCampo.toDouble(campoTemp)
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
                    onValueChange = { novo -> campoTemp = PesoCampo.sanitize(novo) },
                    singleLine = true,
                    label = { Text("Peso (%)") },
                    placeholder = { Text("Ex.: 20") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val valorPeso = PesoCampo.toDouble(campoTemp)
                    val alvo = editarPesoDe!!
                    viewModel.salvarPeso(alvo, valorPeso) { ok, err ->
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
                    append("Notas")
                },
                onVoltar = onVoltar
            )
            Spacer(Modifier.height(16.dp))
        },
        contentWindowInsets = ZeroInsets
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Cabeçalho de colunas (alinhado às linhas)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CARD_SIDE_PAD, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reserva o espaço do ícone + gap, para alinhar com as linhas
                Spacer(Modifier.width(ICON_SIZE))
                Spacer(Modifier.width(ICON_GAP))

                Text(
                    "Avaliação",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    "Nota",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    modifier = Modifier.width(COL_NOTA_WIDTH)
                )

                Spacer(Modifier.width(GAP_NOTA_PESO))

                Text(
                    "Peso",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    modifier = Modifier.width(COL_PESO_WIDTH)
                )
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
                        onEditarAvaliacao = {
                            av.id?.let { onEditarAvaliacao(it.toString(), disciplinaId) }
                        },
                        onEditNota = {
                            campoTemp = NotaCampo.fromDouble(av.nota)
                            editarNotaDe = av
                        },
                        onEditPeso = {
                            campoTemp = PesoCampo.fromDouble(av.peso)
                            editarPesoDe = av
                        }
                    )
                }

                // Cart do “Nota geral”
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outline), // borda sutil
                                shape = RoundedCornerShape(14.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = PesoNotasColor),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        // sem padding horizontal aqui — o Row abaixo usa o mesmo padding das linhas/cabeçalho
                        Column(Modifier.padding(vertical = 10.dp)) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = CARD_SIDE_PAD, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Reserva espaço do ícone (24dp) + gap (10dp) para alinhar com as linhas
                                Spacer(Modifier.width(ICON_SIZE))
                                Spacer(Modifier.width(ICON_GAP))

                                // "Título" da linha
                                Text(
                                    "Nota geral",
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )

                                // Valor de NOTA (alinhado à coluna "Nota")
                                Text(
                                    text = NotaCampo.formatListValue(ui.notaGeral),
                                    modifier = Modifier.width(COL_NOTA_WIDTH),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )

                                Spacer(Modifier.width(GAP_NOTA_PESO))

                                // Valor de PESO (alinhado à coluna "Peso")
                                Text(
                                    text = PesoCampo.formatTotal(ui.somaPesosTotal),
                                    modifier = Modifier.width(COL_PESO_WIDTH),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            // Mensagem auxiliar (mantive central e em destaque)
                            if (ui.faltandoParaAprovacao > 0.0) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "FALTA ${NotaCampo.formatListValue(ui.faltandoParaAprovacao)} PARA APROVAÇÃO!",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = CARD_SIDE_PAD),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
    onEditarAvaliacao: () -> Unit,
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
            Icon(
                Icons.Outlined.Edit,
                contentDescription = "Editar avaliação",
                modifier = Modifier
                    .size(ICON_SIZE)
                    .clickable(onClick = onEditarAvaliacao)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                av.descricao ?: "[sem descrição]",
                modifier = Modifier.weight(1f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )

            // NOTA (clicável)
            Text(
                text = NotaCampo.formatListValue(av.nota),
                modifier = Modifier
                    .width(COL_NOTA_WIDTH)       // <-- fixo
                    .clickable { onEditNota() },
                fontWeight = FontWeight.Normal,
                textAlign = androidx.compose.ui.text.style.TextAlign.End // <-- alinhar à direita
            )
            Spacer(Modifier.width(GAP_NOTA_PESO))

            // PESO (clicável)
            Text(
                text = PesoCampo.formatListValue(av.peso),
                modifier = Modifier
                    .width(COL_PESO_WIDTH)
                    .clickable { onEditPeso() },
                fontWeight = FontWeight.Normal,
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )

            Spacer(Modifier.width(6.dp))

        }
    }
}
