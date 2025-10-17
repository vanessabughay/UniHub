package com.example.unihub.ui.ManterTarefa

import android.app.DatePickerDialog
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
import com.example.unihub.components.Header
import com.example.unihub.data.model.Status
import com.example.unihub.data.model.Priority
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

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val comentarioDateFormat = remember { SimpleDateFormat("dd/MM/yy - HH:mm", Locale.getDefault()) }

    LaunchedEffect(key1 = quadroId) {
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
        val calendar = Calendar.getInstance().apply { timeInMillis = prazo }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                prazo = selectedCalendar.timeInMillis
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
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
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header(
                titulo = if (isEditing) "Editar tarefa" else "Cadastrar tarefa",
                onVoltar = { navController.popBackStack() }
            )

            CampoFormulario(
                label = "Título",
                value = titulo,
                onValueChange = { titulo = it },
                singleLine = true
            )
            CampoDropdownMultiSelect(
                label = "Responsável",
                options = responsaveisDisponiveis,
                selectedIds = responsaveisSelecionados,
                onSelectionChange = tarefaViewModel::atualizarResponsaveisSelecionados,
                placeholder = if (responsaveisDisponiveis.isEmpty()) "Nenhum membro disponível" else "Selecione os responsáveis",
                enabled = responsaveisDisponiveis.isNotEmpty()
            )

            CampoData(
                label = "Prazo",
                value = dateFormat.format(Date(prazo)),
                onClick = { showDatePicker() }
            )

            CampoFormulario(
                label = "Descrição",
                value = descricao,
                onValueChange = { descricao = it })

            if (isEditing) {
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

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD9F6DF))
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
                                                containerColor = Color(
                                                    0xFFD9F6DF
                                                )
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
                                                        color = Color.Gray,
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
        }
    }


private enum class TarefaFormAction {
    CREATE,
    UPDATE,
    DELETE
}









class FakeTarefaRepository : TarefaRepository(object : TarefaApi {
    override suspend fun getTarefa(quadroId: String, colunaId: String, tarefaId: String): Tarefa {
        // Retorna um objeto de tarefa mockado para a prévia
        return Tarefa(
            id = tarefaId,
            descricao = "Esta é uma descrição de exemplo.",
            status = Status.INICIADA,
            prazo = System.currentTimeMillis() + 86400000,
            dataInicio = System.currentTimeMillis(),
            responsaveisIds = listOf(1L)
        )
    }

    override suspend fun createTarefa(
        quadroId: String,
        colunaId: String,
        tarefa: com.example.unihub.data.dto.TarefaPlanejamentoRequestDto
    ) {
        // Nada a ser feito aqui para o mock
    }

    override suspend fun updateTarefa(
        quadroId: String,
        colunaId: String,
        tarefaId: String,
        tarefa: com.example.unihub.data.dto.AtualizarTarefaPlanejamentoRequestDto
    ): Tarefa {
        val status = tarefa.status?.let { Status.valueOf(it) } ?: Status.INICIADA
        return Tarefa(
            id = tarefaId,
            titulo = tarefa.titulo ?: "",
            descricao = tarefa.descricao,
            status = status,
            prazo = tarefa.prazo ?: System.currentTimeMillis(),
            dataInicio = tarefa.dataInicio ?: System.currentTimeMillis(),
            dataFim = tarefa.dataFim,
            responsaveisIds = tarefa.responsavelIds
        )
    }

    override suspend fun deleteTarefa(quadroId: String, colunaId: String, tarefaId: String) {
        // Nada a ser feito aqui
    }

    override suspend fun getComentarios(
        quadroId: String,
        colunaId: String,
        tarefaId: String
    ): ComentariosResponse {
        return ComentariosResponse(
            comentarios = emptyList(),
            receberNotificacoes = true
        )
    }

    override suspend fun createComentario(
        quadroId: String,
        colunaId: String,
        tarefaId: String,
        comentario: com.example.unihub.data.dto.ComentarioRequestDto
    ) = Comentario(
        id = "1",
        conteudo = comentario.conteudo,
        autorId = 1L,
        autorNome = "Usuário",
        isAutor = true,
        dataCriacao = System.currentTimeMillis(),
        dataAtualizacao = System.currentTimeMillis()
    )

    override suspend fun updateComentario(
        quadroId: String,
        colunaId: String,
        tarefaId: String,
        comentarioId: String,
        comentario: com.example.unihub.data.dto.ComentarioRequestDto
    ) = Comentario(
        id = comentarioId,
        conteudo = comentario.conteudo,
        autorId = 1L,
        autorNome = "Usuário",
        isAutor = true,
        dataCriacao = System.currentTimeMillis(),
        dataAtualizacao = System.currentTimeMillis()
    )

    override suspend fun deleteComentario(
        quadroId: String,
        colunaId: String,
        tarefaId: String,
        comentarioId: String
    ) {
        // Nada para remover no mock
    }

    override suspend fun updateComentarioPreference(
        quadroId: String,
        colunaId: String,
        tarefaId: String,
        request: com.example.unihub.data.dto.ComentarioNotificacaoRequestDto
    ) = ComentarioPreferenciaResponse(request.receberNotificacoes)
}) {
    // Essa classe pode ficar vazia, já que a lógica de mock está na interface.
}

// Uma fábrica de ViewModel falsa para o preview, que injeta o repositório falso.
class FakeTarefaFormViewModelFactory : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TarefaFormViewModel::class.java)) {
            val tarefaRepository = FakeTarefaRepository()
            val quadroRepository = QuadroRepository(object : _quadrobackend {
                override suspend fun getQuadrosApi(): List<Quadro> = emptyList()
                override suspend fun getQuadroByIdApi(id: String): Quadro? =
                    Quadro(id = id, nome = "Quadro Preview", grupoId = 1L)
                override suspend fun addQuadroApi(quadro: Quadro) {}
                override suspend fun updateQuadroApi(id: Long, quadro: Quadro): Boolean = true
                override suspend fun deleteQuadroApi(id: Long): Boolean = true
            })

            val grupoRepository = GrupoRepository(object : Grupobackend {
                override suspend fun getGrupoApi(): List<Grupo> = emptyList()
                override suspend fun getGrupoByIdApi(id: String): Grupo? = Grupo(
                    id = id.toLong(),
                    nome = "Grupo Preview",
                    membros = listOf(
                        Contato(id = 1L, nome = "Ana", email = "ana@example.com", pendente = false),
                        Contato(id = 2L, nome = "Bruno", email = "bruno@example.com", pendente = false),
                        Contato(id = 3L, nome = "Carla", email = "carla@example.com", pendente = false)
                    )
                )

                override suspend fun addGrupoApi(grupo: Grupo) {}
                override suspend fun updateGrupoApi(id: Long, grupo: Grupo): Boolean = true
                override suspend fun deleteGrupoApi(id: Long): Boolean = true
            })

            val contatoRepository = ContatoRepository(object : Contatobackend {
                override suspend fun getContatoResumoApi(): List<ContatoResumo> = emptyList()
                override suspend fun getContatoByIdApi(id: String): Contato? =
                    Contato(id = id.toLong(), nome = "Contato $id", email = "contato$id@example.com", pendente = false)
                override suspend fun addContatoApi(contato: Contato) {}
                override suspend fun updateContatoApi(id: Long, contato: Contato): Boolean = true
                override suspend fun deleteContatoApi(id: Long): Boolean = true
                override suspend fun getConvitesPendentesPorEmail(email: String): List<ContatoResumo> = emptyList()
            })

            @Suppress("UNCHECKED_CAST")
            return TarefaFormViewModel(
                repository = tarefaRepository,
                quadroRepository = quadroRepository,
                grupoRepository = grupoRepository,
                contatoRepository = contatoRepository
            ) as T
        }
        throw IllegalArgumentException("Classe de ViewModel desconhecida")
    }
}

// As suas funções de pré-visualização.
@Preview(showBackground = true)
@Composable
fun TarefaFormScreenPreview() {
    TarefaFormScreen(
        navController = rememberNavController(),
        quadroId = "id-do-quadro-exemplo",
        colunaId = "id-da-coluna-exemplo",
        tarefaId = null,
        viewModelFactory = FakeTarefaFormViewModelFactory()
    )
}

@Preview(showBackground = true)
@Composable
fun TarefaFormScreenEditingPreview() {
    TarefaFormScreen(
        navController = rememberNavController(),
        quadroId = "id-do-quadro-exemplo",
        colunaId = "id-da-coluna-exemplo",
        tarefaId = "id-da-tarefa-exemplo",
        viewModelFactory = FakeTarefaFormViewModelFactory()
    )
}