package com.example.notificacoes.ui

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.data.model.Antecedencia
import com.example.unihub.data.model.Prioridade
import com.example.unihub.ui.Notificacoes.NotificacoesUiState
import com.example.unihub.ui.Notificacoes.NotificacoesViewModel
import com.example.unihub.ui.Shared.ZeroInsets


@Composable
fun CardContainer(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF2F3F4),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(Modifier.padding(padding), content = content)
    }
}

@Composable
fun SectionHeaderRow(
    leading: @Composable () -> Unit,
    titulo: String,
    trailing: @Composable () -> Unit = {},
    onClick: (() -> Unit)? = null,
) {
    val row = @Composable {
        Row(
            Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading()
            Spacer(Modifier.width(12.dp))
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            trailing()
        }
    }
    row()
}

@Composable
fun ToggleRow(
    titulo: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF59C36A),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCFD3D7)
            )
        )
    }
}

@Composable
fun DropdownPill(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    minHeight: Dp = 48.dp,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        Surface(
            modifier = Modifier
                .heightIn(min = minHeight)
                .wrapContentWidth()
                .clip(RoundedCornerShape(24.dp))
                .then(if (enabled) Modifier.clickable { expanded = true } else Modifier),
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, Color(0xFF74777A))
        ) {
            Row(
                Modifier
                    .wrapContentWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(options.getOrNull(selectedIndex) ?: label, style = MaterialTheme.typography.titleMedium)
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF3C3F41)
                )
            }
        }

        DropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, text ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelected(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomSaveBar(
    enabled: Boolean,
    onSalvar: () -> Unit
) {
    Surface(color = Color.Transparent) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            val shape = MaterialTheme.shapes.large
            val bg = if (enabled) Color(0xFFCDD1D4) else Color(0xFFD9DDDF)
            Button(
                onClick = onSalvar,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = bg,
                    disabledContainerColor = Color(0xFFD9DDDF),
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp)
            ) {
                Text("Salvar configurações")
            }
        }
    }
}


@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacoesScreen(
    viewModel: NotificacoesViewModel,
    onBack: () -> Unit = {},
    onAbrirHistorico: () -> Unit = {}
) {
    val state by viewModel.ui.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.configuracoesSalvas) {
        if (state.configuracoesSalvas) {
            Toast.makeText(context, "Configurações salvas!", Toast.LENGTH_SHORT).show()
            viewModel.confirmarConfiguracoesSalvas()
        }
    }
    Scaffold(
        topBar = {
            CabecalhoAlternativo(
                titulo = "Gerenciar Notificações",
                onVoltar = onBack
            )
        },
        bottomBar = {
            BottomSaveBar(
                enabled = state.botaoSalvarHabilitado && !state.isLoading,
                onSalvar = { viewModel.salvar() }
            )
        },
        contentWindowInsets = ZeroInsets
    ) { inner ->
        Box(Modifier.padding(inner)) {
            if (state.isLoading && state.original == state.edit) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Content(
                    state = state,
                    onAlternarDisciplinasExpandido = { viewModel.alternarDisciplinasExpandido() },
                    onAlternarQuadrosExpandido = { viewModel.alternarQuadrosExpandido() },
                    onAlternarContatosExpandido = { viewModel.alternarContatosExpandido() },
                    onTogglePresenca = { viewModel.setPresenca(it) },
                    onToggleAvaliacoes = { viewModel.setAvaliacoesAtivas(it) },
                    onSelectAntecedencia = { p, a -> viewModel.setAntecedencia(p, a) },
                    onAbrirHistorico = onAbrirHistorico,
                    onSetCompartilhamentoDisciplina = { viewModel.setCompartilhamentoDisciplina(it) },
                    onSetIncluirEmQuadro = { viewModel.setIncluirEmQuadro(it) },
                    onSetPrazoTarefa = { viewModel.setPrazoTarefa(it) },
                    onSetComentarioTarefa = { viewModel.setComentarioTarefa(it) },
                    onSetConviteContato = { viewModel.setConviteContato(it) },
                    onSetInclusoEmGrupo = { viewModel.setInclusoEmGrupo(it) }
                )
            }
        }
    }
}


@Composable
private fun Content(
    state: NotificacoesUiState,
    onAlternarDisciplinasExpandido: () -> Unit,
    onAlternarQuadrosExpandido: () -> Unit,
    onAlternarContatosExpandido: () -> Unit,
    onTogglePresenca: (Boolean) -> Unit,
    onToggleAvaliacoes: (Boolean) -> Unit,
    onSelectAntecedencia: (Prioridade, Antecedencia) -> Unit,
    onAbrirHistorico: () -> Unit,
    onSetCompartilhamentoDisciplina: (Boolean) -> Unit,
    onSetIncluirEmQuadro: (Boolean) -> Unit,
    onSetPrazoTarefa: (Boolean) -> Unit,
    onSetComentarioTarefa: (Boolean) -> Unit,
    onSetConviteContato: (Boolean) -> Unit,
    onSetInclusoEmGrupo: (Boolean) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = Color(0xFF5E6367),
            shape = MaterialTheme.shapes.large
        ) {
            Row(
                Modifier
                    .clickable { onAbrirHistorico() }
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.History, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(12.dp))
                Text(
                    "Histórico de notificações",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color.White)
            }
        }

        Spacer(Modifier.height(16.dp))


        // SEÇÃO 1: DISCIPLINAS
        CardContainer {
            SectionHeaderRow(
                leading = { Icon(Icons.Outlined.StickyNote2, contentDescription = null) },
                titulo = "Disciplinas",
                trailing = {
                    Icon(
                        if (state.disciplinasExpandido) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null
                    )
                },
                onClick = onAlternarDisciplinasExpandido
            )

            AnimatedVisibility(visible = state.disciplinasExpandido) {
                Column(Modifier.padding(top = 10.dp)) {
                    ToggleRow(
                        titulo = "Presença",
                        checked = state.edit.notificacaoDePresenca,
                        onCheckedChange = onTogglePresenca,
                        enabled = !state.isLoading
                    )

                    // Notificação de compartilhamento de disciplina
                    Spacer(Modifier.height(10.dp))
                    ToggleRow(
                        titulo = "Compartilhamento de disciplina",
                        checked = state.edit.compartilhamentoDisciplina,
                        onCheckedChange = onSetCompartilhamentoDisciplina,
                        enabled = !state.isLoading
                    )

                    Spacer(Modifier.height(10.dp))
                    ToggleRow(
                        titulo = "Avaliações",
                        checked = state.edit.avaliacoesAtivas,
                        onCheckedChange = onToggleAvaliacoes,
                        enabled = !state.isLoading
                    )


                    if (state.edit.avaliacoesAtivas) {
                        Spacer(Modifier.height(16.dp))
                        Text("Antecedência", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF4A4E52))
                        Spacer(Modifier.height(10.dp))

                        val mapa = state.edit.avaliacoesConfig.antecedencia
                        val opcoes = Antecedencia.todas.map { it.label }

                        @Composable
                        fun Linha(p: Prioridade) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(p.displayName, style = MaterialTheme.typography.bodyLarge)
                                DropdownPill(
                                    label = opcoes.first(),
                                    options = opcoes,
                                    selectedIndex = Antecedencia.todas.indexOf(mapa[p] ?: Antecedencia.padrao),
                                    onSelected = { idx -> onSelectAntecedencia(p, Antecedencia.todas[idx]) },
                                    enabled = !state.isLoading
                                )
                            }
                        }

                        Linha(Prioridade.MUITO_ALTA)
                        Linha(Prioridade.ALTA)
                        Linha(Prioridade.MEDIA)
                        Linha(Prioridade.BAIXA)
                        Linha(Prioridade.MUITO_BAIXA)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // SEÇÃO 2: QUADROS/TAREFAS
        CardContainer {
            SectionHeaderRow(
                leading = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                titulo = "Quadros",
                trailing = {
                    Icon(
                        if (state.quadrosExpandido) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null
                    )
                },
                onClick = onAlternarQuadrosExpandido
            )
            AnimatedVisibility(visible = state.quadrosExpandido) {
                Column(Modifier.padding(top = 10.dp)) {

                    ToggleRow(
                        titulo = "Inclusão em um quadro",
                        checked = state.edit.incluirEmQuadro,
                        onCheckedChange = onSetIncluirEmQuadro,
                        enabled = !state.isLoading
                    )
                    Spacer(Modifier.height(10.dp))

                    ToggleRow(
                        titulo = "Prazo de entrega de tarefa",
                        checked = state.edit.prazoTarefa,
                        onCheckedChange = onSetPrazoTarefa,
                        enabled = !state.isLoading
                    )
                    Spacer(Modifier.height(10.dp))

                    ToggleRow(
                        titulo = "Comentários de tarefa",
                        checked = state.edit.comentarioTarefa,
                        onCheckedChange = onSetComentarioTarefa,
                        enabled = !state.isLoading
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // SEÇÃO 3: CONTATOS/GRUPOS
        CardContainer {
            SectionHeaderRow(
                leading = { Icon(Icons.Outlined.Group, contentDescription = null) },
                titulo = "Contatos",
                trailing = {
                    Icon(
                        if (state.contatosExpandido) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null
                    )
                },
                onClick = onAlternarContatosExpandido
            )
            AnimatedVisibility(visible = state.contatosExpandido) {
                Column(Modifier.padding(top = 10.dp)) {

                    ToggleRow(
                        titulo = "Convite como contato",
                        checked = state.edit.conviteContato,
                        onCheckedChange = onSetConviteContato,
                        enabled = !state.isLoading
                    )
                    Spacer(Modifier.height(10.dp))


                    ToggleRow(
                        titulo = "Inclusão em grupo",
                        checked = state.edit.inclusoEmGrupo,
                        onCheckedChange = onSetInclusoEmGrupo,
                        enabled = !state.isLoading
                    )
                }
            }
        }

        Spacer(Modifier.height(96.dp))
    }
}