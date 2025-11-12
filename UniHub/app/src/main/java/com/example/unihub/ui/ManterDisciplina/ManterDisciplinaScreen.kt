package com.example.unihub.ui.ManterDisciplina

import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale
import java.util.UUID
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.components.CampoDisciplina
import com.example.unihub.components.CampoData
import com.example.unihub.components.CampoHorario
import com.example.unihub.components.formatDateToLocale
import com.example.unihub.components.showLocalizedDatePicker
import com.example.unihub.ui.Shared.ZeroInsets


val FormCardColor = Color(0xFFD9EDF6)
val ButtonConfirmColor = Color(0xFF5AB9D6)


private const val PERIOD_MASK_PATTERN = "####/#"
private const val PHONE_MASK_PATTERN = "(##) #####-####"


class MaskVisualTransformation(val mask: String, val maskChar: Char = '#') : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val cleanText = text.text
        val maskedText = StringBuilder()
        var textIndex = 0


        mask.forEach { maskC ->
            if (textIndex >= cleanText.length) {
                return@forEach
            }

            if (maskC == maskChar) {

                maskedText.append(cleanText[textIndex])
                textIndex++
            } else {
                maskedText.append(maskC)
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var transformed = 0
                var original = 0
                mask.forEach { maskC ->
                    if (original >= offset) return transformed
                    if (maskC == maskChar) {
                        original++
                    }
                    transformed++
                    if (transformed > maskedText.length) return transformed
                }
                return transformed
            }

            override fun transformedToOriginal(offset: Int): Int {
                var original = 0
                var transformed = 0
                mask.forEach { maskC ->
                    if (transformed >= offset) return original
                    if (maskC == maskChar) {
                        original++
                    }
                    transformed++
                }
                return original
            }
        }

        return TransformedText(AnnotatedString(maskedText.toString()), offsetMapping)
    }
}


data class AulaInfo(
    val id: Int = UUID.randomUUID().hashCode(),
    var dia: String = "Segunda-feira",
    var ensalamento: String = "",
    var horarioInicio: Int = 0,
    var horarioFim: Int = 0
)

@Composable
fun CampoDeTextoComTitulo(
    titulo: String,
    valor: String,
    onValorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoSelecaoDia(
    diaSelecionado: String,
    onDiaSelecionado: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dias =
        listOf("Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado", "Domingo")
    var expandido by remember { mutableStateOf(false) }

    Column(modifier) {
        Text(
            text = "Dia da Semana",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expandido,
            onExpandedChange = { expandido = !expandido }
        ) {
            OutlinedTextField(
                value = diaSelecionado,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                dias.forEach { dia ->
                    DropdownMenuItem(text = { Text(dia) }, onClick = { onDiaSelecionado(dia); expandido = false })
                }
            }
        }
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun ManterDisciplinaScreen(
    disciplinaId: String?,
    onVoltar: () -> Unit,
    onExcluirSucesso: () -> Unit,
    viewModel: ManterDisciplinaViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    var isExclusao by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val locale = remember { Locale("pt", "BR") }

    LaunchedEffect(Unit) {
        viewModel.carregarFrequenciaMinima()
    }

    LaunchedEffect(disciplinaId) {
        if (disciplinaId != null) {
            viewModel.loadDisciplina(disciplinaId)
        }
    }

    LaunchedEffect(uiState.sucesso) {
        if (uiState.sucesso) {
            if (isExclusao) {
                onExcluirSucesso()
            } else {
                onVoltar()
            }
        }
    }

    uiState.erro?.let {
        Toast.makeText(context, "Erro: $it", Toast.LENGTH_LONG).show()
    }


    Scaffold(
        topBar = {
            CabecalhoAlternativo(
                titulo = if (disciplinaId == null) "Nova Disciplina" else "Editar Disciplina",
                onVoltar = onVoltar
            )
        },
        contentWindowInsets = ZeroInsets
    ) { paddingValues ->
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        isExclusao = true
                        disciplinaId?.let { viewModel.deleteDisciplina(it) }
                    }) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("Confirmar exclusão") },
                text = { Text("Tem certeza de que deseja excluir esta disciplina? Essa ação não pode ser desfeita.") }
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                CampoDisciplina(title = "Informações Gerais") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoDeTextoComTitulo(
                            "Código da Disciplina",
                            uiState.codigo,
                            { viewModel.updateField(it, { s -> s.codigo }, { s, v -> s.copy(codigo = v) }) },
                            Modifier.weight(1f),
                            placeholder = "DSXXX"
                        )

                        CampoDeTextoComTitulo(
                            titulo = "Período",
                            valor = uiState.periodo,
                            onValorChange = {
                                val digitos = it.filter { c -> c.isDigit() }
                                if (digitos.length <= 5) {
                                    viewModel.updateField(digitos, { s -> s.periodo }, { s, v -> s.copy(periodo = v) })
                                }
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = "20XX/X",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = MaskVisualTransformation(PERIOD_MASK_PATTERN)
                        )
                    }
                    CampoDeTextoComTitulo(
                        "Nome da Disciplina",
                        uiState.nomeDisciplina,
                        { viewModel.updateField(it, { s -> s.nomeDisciplina }, { s, v -> s.copy(nomeDisciplina = v) }) },
                        placeholder = "Disciplina X"
                    )
                    CampoDeTextoComTitulo(
                        "Nome do Professor",
                        uiState.nomeProfessor,
                        { viewModel.updateField(it, { s -> s.nomeProfessor }, { s, v -> s.copy(nomeProfessor = v) }) },
                        placeholder = "Professor X"
                    )
                }
            }

            item {
                CampoDisciplina(title = "Informações de Aula") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        CampoDeTextoComTitulo(
                            titulo = "Carga Horária",
                            valor = uiState.cargaHoraria,
                            onValorChange = {
                                viewModel.updateField(
                                    it.filter { c -> c.isDigit() },
                                    { s -> s.cargaHoraria },
                                    { s, v -> s.copy(cargaHoraria = v) }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = "Ex: 60",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        CampoDeTextoComTitulo(
                            "Aulas/Semana",
                            uiState.qtdAulasSemana,
                            {
                                val filtered = it.filter { c -> c.isDigit() }
                                viewModel.updateQtdAulasSemana(filtered)
                            },
                            Modifier.weight(1f),
                            placeholder = "Ex: 2",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        CampoDeTextoComTitulo(
                            "Semanas (Total)",
                            uiState.qtdSemanas,
                            {
                                viewModel.updateField(
                                    it.filter { c -> c.isDigit() },
                                    { s -> s.qtdSemanas },
                                    { s, v -> s.copy(qtdSemanas = v) }
                                )
                            },
                            Modifier.weight(1f),
                            placeholder = "Ex: 18",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        CampoDeTextoComTitulo(
                            "Limite de Ausências",
                            uiState.ausenciasPermitidas,
                            {
                                viewModel.updateField(
                                    it.filter { c -> c.isDigit() },
                                    { s -> s.ausenciasPermitidas },
                                    { s, v -> s.copy(ausenciasPermitidas = v) }
                                )
                            },
                            Modifier.weight(1f),
                            placeholder = "Ex: 4",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    uiState.aulas.forEachIndexed { index, aula ->
                        CampoSelecaoDia(
                            diaSelecionado = aula.dia,
                            onDiaSelecionado = { novoValor ->
                                viewModel.updateAula(index) { it.copy(dia = novoValor) }
                            }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CampoDeTextoComTitulo(
                                titulo = "Ensalamento",
                                valor = aula.ensalamento,
                                onValorChange = { novoValor ->
                                    viewModel.updateAula(index) { it.copy(ensalamento = novoValor) }
                                },
                                modifier = Modifier.weight(1f),
                                placeholder = "Ex: B05"
                            )
                            CampoHorario(
                                "Início",
                                aula.horarioInicio,
                                { novoValor ->
                                    viewModel.updateAula(index) { it.copy(horarioInicio = novoValor) }
                                },
                                Modifier.weight(1f)
                            )
                            CampoHorario(
                                "Fim",
                                aula.horarioFim,
                                { novoValor ->
                                    viewModel.updateAula(index) { it.copy(horarioFim = novoValor) }
                                },
                                Modifier.weight(1f)
                            )
                        }
                        if (index < uiState.aulas.size - 1) HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoData(
                            label = "Início do Semestre",
                            value = formatDateToLocale(uiState.dataInicioSemestre, locale),
                            onClick = {
                                showLocalizedDatePicker(context, uiState.dataInicioSemestre, locale) {
                                    viewModel.setDataInicioSemestre(it)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        CampoData(
                            label = "Fim do Semestre",
                            value = formatDateToLocale(uiState.dataFimSemestre, locale),
                            onClick = {
                                showLocalizedDatePicker(context, uiState.dataFimSemestre, locale) {
                                    viewModel.setDataFimSemestre(it)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                }
            }

            item {
                CampoDisciplina(title = "Informações do Professor") {
                    CampoDeTextoComTitulo(
                        titulo = "E-mail",
                        valor = uiState.emailProfessor,
                        onValorChange = {
                            viewModel.updateField(it, { s -> s.emailProfessor }, { s, v -> s.copy(emailProfessor = v) })
                        },
                        placeholder = "professor@exemplo.com",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    CampoDeTextoComTitulo(
                        titulo = "Plataformas utilizadas",
                        valor = uiState.plataformas,
                        onValorChange = {
                            viewModel.updateField(it, { s -> s.plataformas }, { s, v -> s.copy(plataformas = v) })
                        },
                        placeholder = "Ex: Moodle, Teams"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoDeTextoComTitulo(
                            titulo = "Telefone",
                            valor = uiState.telefoneProfessor,
                            onValorChange = {
                                val digitos = it.filter { c -> c.isDigit() }
                                if (digitos.length <= 11) {
                                    viewModel.updateField(digitos, { s -> s.telefoneProfessor }, { s, v -> s.copy(telefoneProfessor = v) })
                                }
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = "(XX) XXXXX-XXXX",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            visualTransformation = MaskVisualTransformation(PHONE_MASK_PATTERN)
                        )
                        CampoDeTextoComTitulo(
                            titulo = "Sala do Professor",
                            valor = uiState.salaProfessor,
                            onValorChange = {
                                viewModel.updateField(it, { s -> s.salaProfessor }, { s, v -> s.copy(salaProfessor = v) })
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = "Ex: D20"
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Disciplina Ativa", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = uiState.isAtiva,
                        onCheckedChange = {
                            Log.d("DISCIPLINA_DEBUG", "Switch clicado. Novo valor: $it")
                            viewModel.setIsAtiva(it)
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = ButtonConfirmColor)
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        if (!uiState.isLoading) {
                            viewModel.saveDisciplina()
                        }
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonConfirmColor)
                ) {
                    Text(
                        text = if (uiState.isLoading) "Salvando..." else "Confirmar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            item {
                if (disciplinaId != null) {
                    OutlinedButton(
                        onClick = { showDialog = true },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFE91E1E))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Excluir Disciplina", color = Color(0xFFE91E1E))
                    }
                }}

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}