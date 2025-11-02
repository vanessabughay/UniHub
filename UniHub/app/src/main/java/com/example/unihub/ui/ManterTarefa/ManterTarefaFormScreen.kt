package com.example.unihub.ui.ManterTarefa

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.unihub.components.BotoesFormulario
import com.example.unihub.components.CampoCombobox
import com.example.unihub.components.CampoDropdownMultiSelect
import com.example.unihub.components.CampoData
import com.example.unihub.components.CampoFormulario

import com.example.unihub.data.model.Status
import com.example.unihub.data.model.Comentario
import com.example.unihub.data.model.ComentarioPreferenciaResponse
import com.example.unihub.data.model.ComentariosResponse
import com.example.unihub.data.model.Tarefa
import java.text.SimpleDateFormat
import java.util.*
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.ContatoResumo
import com.example.unihub.data.repository.Grupobackend
import com.example.unihub.data.repository.GrupoRepository
import com.example.unihub.data.repository.QuadroRepository
import com.example.unihub.data.repository.TarefaRepository
import com.example.unihub.data.repository._quadrobackend
import com.example.unihub.data.api.TarefaApi
import com.example.unihub.data.repository.Contatobackend
import com.example.unihub.data.model.Contato
import com.example.unihub.data.model.Grupo
import com.example.unihub.data.model.Quadro
import com.example.unihub.components.formatDateToLocale
import com.example.unihub.components.showLocalizedDatePicker
import java.util.Calendar
import java.util.Locale
import com.example.unihub.components.CabecalhoAlternativo


private fun getDefaultPrazoForUI(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

@Composable
fun TarefaFormScreen(
    navController: NavHostController,
    quadroId: String,
    colunaId: String,
    tarefaId: String? = null,
    viewModelFactory: ViewModelProvider.Factory
) {

    val tarefaViewModel: TarefaFormViewModel = viewModel(factory = viewModelFactory)

    val context = LocalContext.current
    val isEditing = tarefaId != null
    val tarefaState by tarefaViewModel.tarefa.collectAsState()
    val isLoading by tarefaViewModel.isLoading.collectAsState()
    val formResult by tarefaViewModel.formResult.collectAsState()
    val responsaveisDisponiveis by tarefaViewModel.responsaveisDisponiveis.collectAsState()
    val responsaveisSelecionados by tarefaViewModel.responsaveisSelecionados.collectAsState()
    val comentarios by tarefaViewModel.comentarios.collectAsState()
    val comentariosCarregando by tarefaViewModel.comentariosCarregando.collectAsState()
    val receberNotificacoesComentarios by tarefaViewModel.receberNotificacoes.collectAsState()
    val comentarioResultado by tarefaViewModel.comentarioResultado.collectAsState()

    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var statusSelecionado by remember { mutableStateOf(Status.INICIADA) }
    var prazo by remember { mutableStateOf(getDefaultPrazoForUI()) }
    var ultimaAcao by remember { mutableStateOf<TarefaFormAction?>(null) }
    var novoComentario by remember { mutableStateOf("") }
    var comentarioEmEdicaoId by remember { mutableStateOf<String?>(null) }
    var textoComentarioEdicao by remember { mutableStateOf("") }
    var comentarioParaExcluir by remember { mutableStateOf<String?>(null) }

val locale = remember { Locale("pt", "BR") }
val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", locale) }
val comentarioDateFormat = remember { SimpleDateFormat("dd/MM/yy - HH:mm", locale) }



LaunchedEffect(quadroId) {
    tarefaViewModel.carregarResponsaveis(quadroId)
}


    LaunchedEffect(key1 = tarefaId) {
        if (isEditing) {
            tarefaViewModel.carregarTarefa(quadroId, colunaId, tarefaId!!)
        }
    }

    LaunchedEffect(key1 = tarefaId, key2 = isEditing) {
        if (isEditing && tarefaId != null) {
            tarefaViewModel.carregarComentarios(quadroId, colunaId, tarefaId)
        }
    }

    LaunchedEffect(key1 = tarefaState) {
        if (isEditing) {
            tarefaState?.let { loadedTarefa ->
                titulo = loadedTarefa.titulo
                descricao = loadedTarefa.descricao ?: ""
                statusSelecionado = loadedTarefa.status
                prazo = loadedTarefa.prazo
                tarefaViewModel.atualizarResponsaveisSelecionados(loadedTarefa.responsaveisIds.toSet())
            }
        }
    }

    LaunchedEffect(formResult) {
        when (val result = formResult) {
            TarefaFormResult.Success -> {
                val mensagem = when (ultimaAcao) {
                    TarefaFormAction.CREATE -> "Tarefa criada com sucesso!"
                    TarefaFormAction.UPDATE -> "Tarefa atualizada com sucesso!"
                    TarefaFormAction.DELETE -> "Tarefa excluída com sucesso!"
                    null -> "Operação realizada com sucesso!"
                }

                Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
                tarefaViewModel.resetFormResult()
                navController.popBackStack()
            }

            is TarefaFormResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                tarefaViewModel.resetFormResult()
            }
            else -> {}
        }
    }

    LaunchedEffect(comentarioResultado) {
        when (val resultado = comentarioResultado) {
            is ComentarioActionResult.Success -> {
                Toast.makeText(context, resultado.message, Toast.LENGTH_SHORT).show()
                if (resultado.clearNewComment) {
                    novoComentario = ""
                }
                if (resultado.resetEditing) {
                    comentarioEmEdicaoId = null
                    textoComentarioEdicao = ""
                }
                tarefaViewModel.resetComentarioResultado()
            }

            is ComentarioActionResult.Error -> {
                Toast.makeText(context, resultado.message, Toast.LENGTH_LONG).show()
                tarefaViewModel.resetComentarioResultado()
            }

            null -> Unit
        }
    }

    val showDatePicker = {
        showLocalizedDatePicker(context, prazo, locale) { prazo = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            CabecalhoAlternativo(
                titulo = if (isEditing) "Editar tarefa" else "Cadastrar tarefa",
                onVoltar = { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoFormulario(
                label = "Título",
                value = titulo,
                onValueChange = { titulo = it },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xE9CFE5D0), shape = RoundedCornerShape(8.dp))
            ){
            CampoDropdownMultiSelect(
                label = "Responsável",
                options = responsaveisDisponiveis,
                selectedIds = responsaveisSelecionados,
                onSelectionChange = tarefaViewModel::atualizarResponsaveisSelecionados,
                placeholder = if (responsaveisDisponiveis.isEmpty()) "Nenhum membro disponível" else "Selecione os responsáveis",
                enabled = responsaveisDisponiveis.isNotEmpty()
            )}

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xE9CFE5D0), shape = RoundedCornerShape(8.dp))
            ){
            CampoData(
                label = "Prazo",
                value = formatDateToLocale(prazo, locale),
                onClick = showDatePicker
            )}

            Spacer(modifier = Modifier.height(16.dp))

            CampoFormulario(
                label = "Descrição",
                value = descricao,
                onValueChange = { descricao = it })

            if (isEditing) {
                Spacer(modifier = Modifier.height(16.dp))
                CampoCombobox(
                    label = "Estado",
                    options = Status.values().toList(),
                    selectedOption = statusSelecionado,
                    onOptionSelected = { statusSelecionado = it },
                    optionToDisplayedString = { status ->
                        status.name.lowercase()
                            .replaceFirstChar { it.titlecase(Locale.getDefault()) }
                    },
                    placeholder = "Selecione o estado"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xE9CFE5D0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Comentários",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            OutlinedTextField(
                                value = novoComentario,
                                onValueChange = { novoComentario = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp),
                                placeholder = { Text("Escrever um comentário") },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFE8FAED),
                                    unfocusedContainerColor = Color(0xFFE8FAED),
                                    focusedBorderColor = Color(0xFF28A745),
                                    unfocusedBorderColor = Color(0xFF28A745).copy(alpha = 0.4f),
                                    cursorColor = Color(0xFF28A745)
                                ),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (novoComentario.isNotBlank()) {
                                                tarefaId?.let { id ->
                                                    tarefaViewModel.criarComentario(
                                                        quadroId,
                                                        colunaId,
                                                        id,
                                                        novoComentario
                                                    )
                                                }
                                            }
                                        },
                                        enabled = novoComentario.isNotBlank()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Enviar comentário",
                                            tint = Color(0xFF28A745)
                                        )
                                    }
                                }
                            )


                            if (comentariosCarregando) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }

                            if (comentarios.isEmpty() && !comentariosCarregando) {
                                Text(
                                    text = "Nenhum comentário ainda.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                        alpha = 0.8f
                                    )
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    comentarios.forEach { comentario ->
                                        val autorLabel = if (comentario.isAutor) {
                                            "${comentario.autorNome} (eu)"
                                        } else {
                                            comentario.autorNome
                                        }
                                        val dataComentario =
                                            comentario.dataAtualizacao ?: comentario.dataCriacao
                                        val dataTexto = dataComentario?.let {
                                            comentarioDateFormat.format(
                                                Date(it)
                                            )
                                        } ?: ""

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0xFFF1F8F1)
                                            ),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.Top,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        if (comentarioEmEdicaoId == comentario.id) {
                                                            OutlinedTextField(
                                                                value = textoComentarioEdicao,
                                                                onValueChange = {
                                                                    textoComentarioEdicao = it
                                                                },
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .heightIn(min = 56.dp),
                                                                shape = RoundedCornerShape(12.dp),
                                                                colors = OutlinedTextFieldDefaults.colors(
                                                                    focusedContainerColor = Color.White,
                                                                    unfocusedContainerColor = Color.White,
                                                                    focusedBorderColor = Color(
                                                                        0xFF28A745
                                                                    ),
                                                                    unfocusedBorderColor = Color(
                                                                        0xFF28A745
                                                                    ).copy(alpha = 0.4f)
                                                                )
                                                            )
                                                        } else {
                                                            Text(
                                                                text = buildAnnotatedString {
                                                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                                                        append("$autorLabel: ")
                                                                    }
                                                                    append(comentario.conteudo)
                                                                },
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                        }
                                                    }

                                                    if (comentario.isAutor) {
                                                        val iconButtonSize = 32.dp
                                                        val iconSize = 18.dp

                                                        Row(
                                                            modifier = Modifier
                                                                .wrapContentWidth(Alignment.End)
                                                                .align(Alignment.Top),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            if (comentarioEmEdicaoId == comentario.id) {
                                                                IconButton(
                                                                    onClick = {
                                                                        if (textoComentarioEdicao.isNotBlank()) {
                                                                            tarefaId?.let { id ->
                                                                                tarefaViewModel.atualizarComentario(
                                                                                    quadroId,
                                                                                    colunaId,
                                                                                    id,
                                                                                    comentario.id,
                                                                                    textoComentarioEdicao
                                                                                )
                                                                            }
                                                                        }
                                                                    },
                                                                    enabled = textoComentarioEdicao.isNotBlank(),
                                                                    modifier = Modifier.size(iconButtonSize)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Check,
                                                                        contentDescription = "Salvar edição",
                                                                        tint = Color(0xFF28A745),
                                                                        modifier = Modifier.size(iconSize)
                                                                    )
                                                                }

                                                                IconButton(
                                                                    onClick = {
                                                                        comentarioEmEdicaoId = null
                                                                        textoComentarioEdicao = ""
                                                                    },
                                                                    modifier = Modifier.size(iconButtonSize)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Close,
                                                                        contentDescription = "Cancelar edição",
                                                                        modifier = Modifier.size(iconSize)
                                                                    )
                                                                }
                                                            } else {
                                                                IconButton(
                                                                    onClick = {
                                                                        comentarioEmEdicaoId = comentario.id
                                                                        textoComentarioEdicao =
                                                                            comentario.conteudo
                                                                    },
                                                                    modifier = Modifier.size(iconButtonSize)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Edit,
                                                                        contentDescription = "Editar comentário",
                                                                        modifier = Modifier.size(iconSize)
                                                                    )
                                                                }

                                                                IconButton(
                                                                    onClick = {
                                                                        comentarioParaExcluir =
                                                                            comentario.id
                                                                    },
                                                                    modifier = Modifier.size(iconButtonSize)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Delete,
                                                                        contentDescription = "Excluir comentário",
                                                                        modifier = Modifier.size(iconSize)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                if (dataTexto.isNotEmpty()) {
                                                    Text(
                                                        text = dataTexto,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.Black.copy(alpha = 0.7f),
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }


                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = receberNotificacoesComentarios,
                        onCheckedChange = { marcado ->
                            tarefaId?.let { id ->
                                tarefaViewModel.atualizarPreferenciaComentarios(
                                    quadroId,
                                    colunaId,
                                    id,
                                    marcado
                                )
                            }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF28A745))
                    )
                    Text("Receber notificações", style = MaterialTheme.typography.bodyMedium)
                }
            }

            comentarioParaExcluir?.let { idParaExcluir ->
                AlertDialog(
                    onDismissRequest = { comentarioParaExcluir = null },
                    title = { Text("Excluir comentário") },
                    text = { Text("Tem certeza de que deseja excluir este comentário?") },
                    confirmButton = {
                        TextButton(onClick = {
                            tarefaId?.let { tarefaAtual ->
                                tarefaViewModel.excluirComentario(
                                    quadroId,
                                    colunaId,
                                    tarefaAtual,
                                    idParaExcluir
                                )
                            }
                            comentarioParaExcluir = null
                        }) {
                            Text("Excluir")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { comentarioParaExcluir = null }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))


        BotoesFormulario(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp),
            onConfirm = {
                if (titulo.isBlank()) {
                    Toast.makeText(context, "O título da tarefa é obrigatório.", Toast.LENGTH_SHORT).show()
                    return@BotoesFormulario
                }

                if (!isEditing) {
                    val novaTarefa = Tarefa(
                        titulo = titulo,
                        descricao = if (descricao.isBlank()) null else descricao,
                        status = Status.INICIADA, // Novas tarefas sempre iniciam com este status
                        prazo = prazo,
                        dataInicio = System.currentTimeMillis()
                    )
                    ultimaAcao = TarefaFormAction.CREATE
                    tarefaViewModel.cadastrarTarefa(quadroId, colunaId, novaTarefa)
                } else {
                    tarefaState?.let { tarefaCarregada ->
                        val tarefaAtualizada = tarefaCarregada.copy(
                            titulo = titulo,
                            descricao = if (descricao.isBlank()) null else descricao,
                            status = statusSelecionado,
                            prazo = prazo
                        )
                        ultimaAcao = TarefaFormAction.UPDATE
                        tarefaViewModel.atualizarTarefa(quadroId, colunaId, tarefaAtualizada)
                    }
                }
            },
            onDelete = if (isEditing) {
                {
                    ultimaAcao = TarefaFormAction.DELETE
                    tarefaId?.let { id -> tarefaViewModel.excluirTarefa(quadroId, colunaId, id) }
                }
            } else null
        )
        Spacer(modifier = Modifier.height(35.dp))
        }
    }


private enum class TarefaFormAction {
    CREATE,
    UPDATE,
    DELETE
}
