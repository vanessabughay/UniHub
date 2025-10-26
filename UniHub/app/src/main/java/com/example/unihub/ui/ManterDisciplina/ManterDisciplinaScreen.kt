package com.example.unihub.ui.ManterDisciplina

import android.os.Build
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
import java.time.format.DateTimeFormatter
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.components.CampoDisciplina
import java.util.Locale
import java.util.UUID
import android.widget.Toast
import androidx.annotation.RequiresExtension
import java.time.ZoneId
import java.time.Instant
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import kotlin.math.floor
import kotlin.math.max
import com.example.unihub.components.CampoData
import com.example.unihub.components.CampoHorario
import com.example.unihub.components.formatDateToLocale
import com.example.unihub.components.showLocalizedDatePicker

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
    var codigo by remember { mutableStateOf("") }
    var periodo by remember { mutableStateOf("") }
    var nomeDisciplina by remember { mutableStateOf("") }
    var nomeProfessor by remember { mutableStateOf("") }
    var cargaHoraria by remember { mutableStateOf("") }
    var qtdSemanas by remember { mutableStateOf("") }
    var qtdAulasSemana by remember { mutableStateOf("1") }
    var dataInicioSemestre by remember { mutableStateOf(0L) }
    var dataFimSemestre by remember { mutableStateOf(0L) }
    var aulas by remember { mutableStateOf(listOf(AulaInfo())) }
    var emailProfessor by remember { mutableStateOf("") }
    var plataformas by remember { mutableStateOf("") }
    var telefoneProfessor by remember { mutableStateOf("") }
    var salaProfessor by remember { mutableStateOf("") }
    var ausenciasPermitidas by remember { mutableStateOf("") }
    var isAtiva by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var isExclusao by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val frequenciaMinima = viewModel.frequenciaMinima.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.carregarFrequenciaMinima()
    }

    LaunchedEffect(qtdSemanas, qtdAulasSemana, frequenciaMinima.value, disciplinaId) {
        if (disciplinaId == null) {
            val freq = frequenciaMinima.value
            val semanas = qtdSemanas.toIntOrNull()
            if (freq != null && semanas != null) {
                val limiteCalculado = floor(((100 - freq).coerceAtLeast(0) / 100.0) * semanas).toInt()
                ausenciasPermitidas = max(limiteCalculado, 0).toString()
            }
        }
    }

    LaunchedEffect(qtdAulasSemana) {
        val quantidade = qtdAulasSemana.toIntOrNull() ?: 0
        if (quantidade > aulas.size) aulas = aulas + List(quantidade - aulas.size) { AulaInfo() }
        else if (quantidade < aulas.size && quantidade >= 0) aulas = aulas.take(quantidade)
    }

    val context = LocalContext.current
    val locale = remember { Locale("pt", "BR") }
    val disciplina = viewModel.disciplina.collectAsState()
    val erro = viewModel.erro.collectAsState()
    val sucesso = viewModel.sucesso.collectAsState()




    LaunchedEffect(disciplinaId) {
        if (disciplinaId != null) {
            viewModel.loadDisciplina(disciplinaId)
        }
    }


    LaunchedEffect(disciplina.value) {
        disciplina.value?.let { d ->
            codigo = d.codigo.orEmpty()
            nomeDisciplina = d.nome.orEmpty()
            nomeProfessor = d.professor.orEmpty()
            periodo = d.periodo.orEmpty()
            cargaHoraria = (d.cargaHoraria ?: 0).toString()
            qtdSemanas = (d.qtdSemanas ?: 0).toString()

            val aulasList = d.aulas.orEmpty()
            qtdAulasSemana = aulasList.size.toString()
            aulas = aulasList.map {
                AulaInfo(
                    dia = it.diaDaSemana,
                    ensalamento = it.sala,
                    horarioInicio = it.horarioInicio,
                    horarioFim = it.horarioFim
                )
            }

            dataInicioSemestre = d.dataInicioSemestre
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli()
                ?: 0L

            dataFimSemestre = d.dataFimSemestre
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli()
                ?: 0L

            emailProfessor = d.emailProfessor.orEmpty()
            plataformas = d.plataforma.orEmpty()
            telefoneProfessor = d.telefoneProfessor.orEmpty()
            salaProfessor = d.salaProfessor.orEmpty()
            ausenciasPermitidas = d.ausenciasPermitidas?.toString() ?: ""

            isAtiva = d.isAtiva
        }
    }



    LaunchedEffect(sucesso.value) {
        if (sucesso.value) {
            if (isExclusao) {
                onExcluirSucesso()
            } else {
                onVoltar()
            }
        }
    }

    erro.value?.let {
        Toast.makeText(context, "Erro: $it", Toast.LENGTH_LONG).show()
    }


    Scaffold(
        topBar = {
            CabecalhoAlternativo(
                titulo = if (disciplinaId == null) "Nova Disciplina" else "Editar Disciplina",
                onVoltar = onVoltar
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        if (!isSaving) {
                            isSaving = true

                            val inicio = Instant.ofEpochMilli(dataInicioSemestre)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            val fim = Instant.ofEpochMilli(dataFimSemestre)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            val ausenciasExistentes = disciplina.value?.ausencias ?: emptyList()
                            val disciplina = com.example.unihub.data.model.Disciplina(
                                id = disciplinaId?.toLongOrNull(),
                                codigo = codigo,
                                nome = nomeDisciplina,
                                professor = nomeProfessor,
                                periodo = periodo,
                                cargaHoraria = cargaHoraria.toIntOrNull() ?: 0,
                                qtdSemanas = qtdSemanas.toIntOrNull() ?: 0,
                                dataInicioSemestre = inicio,
                                dataFimSemestre = fim,
                                emailProfessor = emailProfessor,
                                plataforma = plataformas,
                                telefoneProfessor = telefoneProfessor,
                                salaProfessor = salaProfessor,
                                ausencias = ausenciasExistentes,
                                ausenciasPermitidas = ausenciasPermitidas.toIntOrNull(),
                                isAtiva = isAtiva,
                                receberNotificacoes = true,
                                avaliacoes = emptyList(),
                                aulas = aulas.map {
                                    com.example.unihub.data.model.HorarioAula(
                                        diaDaSemana = it.dia,
                                        sala = it.ensalamento,
                                        horarioInicio = it.horarioInicio,
                                        horarioFim = it.horarioFim
                                    )
                                }
                            )

                            if (disciplinaId == null) {
                                viewModel.createDisciplina(disciplina)
                            } else {
                                viewModel.updateDisciplina(disciplina)
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonConfirmColor)
                ) {
                    Text(
                        text = if (isSaving) "Salvando..." else "Confirmar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    )

    { paddingValues ->
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
                        CampoDeTextoComTitulo("Código da Disciplina", codigo, { codigo = it }, Modifier.weight(1f), placeholder = "DSXXX")

                        CampoDeTextoComTitulo(
                            titulo = "Período",
                            valor = periodo,
                            onValorChange = {
                                val digitos = it.filter { c -> c.isDigit() }
                                if (digitos.length <= 5) {
                                    periodo = digitos
                                }
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = "20XX/X",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = MaskVisualTransformation(PERIOD_MASK_PATTERN)
                        )
                    }
                    CampoDeTextoComTitulo("Nome da Disciplina", nomeDisciplina, { nomeDisciplina = it }, placeholder = "Disciplina X")
                    CampoDeTextoComTitulo("Nome do Professor", nomeProfessor, { nomeProfessor = it }, placeholder = "Professor X")
                }
            }

            item {
                CampoDisciplina(title = "Informações de Aula") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        CampoDeTextoComTitulo(
                            titulo = "Carga Horária",
                            valor = cargaHoraria,
                            onValorChange = { cargaHoraria = it.filter { c -> c.isDigit() } },
                            modifier = Modifier.weight(1f),
                            placeholder = "Ex: 60",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        CampoDeTextoComTitulo(
                            "Aulas/Semana",
                            qtdAulasSemana,
                            { qtdAulasSemana = it.filter { c -> c.isDigit() } },
                            Modifier.weight(1f),
                            placeholder = "Ex: 2",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        CampoDeTextoComTitulo(
                            "Semanas (Total)",
                            qtdSemanas,
                            { qtdSemanas = it.filter { c -> c.isDigit() } },
                            Modifier.weight(1f),
                            placeholder = "Ex: 18",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        CampoDeTextoComTitulo(
                            "Limite de Ausências",
                            ausenciasPermitidas,
                            { ausenciasPermitidas = it.filter { c -> c.isDigit() } },
                            Modifier.weight(1f),
                            placeholder = "Ex: 4",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    aulas.forEachIndexed { index, aula ->
                        CampoSelecaoDia(
                            diaSelecionado = aula.dia,
                            onDiaSelecionado = { novoValor -> aulas = aulas.toMutableList().also { it[index] = aula.copy(dia = novoValor) } }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CampoDeTextoComTitulo(
                                titulo = "Ensalamento",
                                valor = aula.ensalamento,
                                onValorChange = { novoValor -> aulas = aulas.toMutableList().also { it[index] = aula.copy(ensalamento = novoValor) } },
                                modifier = Modifier.weight(1f),
                                placeholder = "Ex: B05"
                            )
                            CampoHorario(
                                "Início",
                                aula.horarioInicio,
                                { novoValor -> aulas = aulas.toMutableList().also { it[index] = aula.copy(horarioInicio = novoValor) } },
                                Modifier.weight(1f)
                            )
                            CampoHorario(
                                "Fim",
                                aula.horarioFim,
                                { novoValor -> aulas = aulas.toMutableList().also { it[index] = aula.copy(horarioFim = novoValor) } },
                                Modifier.weight(1f)
                            )
                        }
                        if (index < aulas.size - 1) HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoData(
                            label = "Início do Semestre",
                            value = formatDateToLocale(dataInicioSemestre, locale),
                            onClick = {
                                showLocalizedDatePicker(context, dataInicioSemestre, locale) {
                                    dataInicioSemestre = it
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        CampoData(
                            label = "Fim do Semestre",
                            value = formatDateToLocale(dataFimSemestre, locale),
                            onClick = {
                                showLocalizedDatePicker(context, dataFimSemestre, locale) {
                                    dataFimSemestre = it
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
                        valor = emailProfessor,
                        onValorChange = { emailProfessor = it },
                        placeholder = "professor@exemplo.com",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    CampoDeTextoComTitulo(
                        titulo = "Plataformas utilizadas",
                        valor = plataformas,
                        onValorChange = { plataformas = it },
                        placeholder = "Ex: Moodle, Teams"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoDeTextoComTitulo(
                            titulo = "Telefone",
                            valor = telefoneProfessor,
                            onValorChange = {
                                val digitos = it.filter { c -> c.isDigit() }
                                if (digitos.length <= 11) {
                                    telefoneProfessor = digitos
                                }
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = "(XX) XXXXX-XXXX",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            visualTransformation = MaskVisualTransformation(PHONE_MASK_PATTERN)
                        )
                        CampoDeTextoComTitulo(
                            titulo = "Sala do Professor",
                            valor = salaProfessor,
                            onValorChange = { salaProfessor = it },
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
                        checked = isAtiva,
                        onCheckedChange = { isAtiva = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = ButtonConfirmColor)
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 80.dp, end = 80.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFE91E1E))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Excluir Disciplina", color = Color(0xFFE91E1E))
                }

            }
        }
    }
}