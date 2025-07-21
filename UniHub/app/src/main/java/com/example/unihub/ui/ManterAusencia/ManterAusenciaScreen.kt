package com.example.unihub.ui.ManterAusencia

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.data.model.Ausencia
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun ManterAusenciaScreen(
    disciplinaId: String,
    onVoltar: () -> Unit,
    viewModel: ManterAusenciaViewModel
) {
    val context = LocalContext.current

    var data by remember { mutableStateOf(LocalDate.now()) }
    var justificativa by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val showDatePicker = {
        val now = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                data = LocalDate.of(year, month + 1, dayOfMonth)
            },
            data.year,
            data.monthValue - 1,
            data.dayOfMonth
        ).show()
    }

    val sucesso by viewModel.sucesso.collectAsState()
    val erro by viewModel.erro.collectAsState()

    LaunchedEffect(sucesso) {
        if (sucesso) onVoltar()
    }

    Scaffold(
        topBar = { CabecalhoAlternativo("Registrar Ausência", onVoltar) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .width(375.dp)
                .height(714.dp)
                .background(Color(0xFFF2F2F2))
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = data.format(formatter),
                onValueChange = {},
                label = { Text("Data da ausência") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker() },
                readOnly = true
            )
            OutlinedTextField(
                value = justificativa,
                onValueChange = { justificativa = it },
                label = { Text("Justificativa") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = categoria,
                onValueChange = { categoria = it },
                label = { Text("Categoria") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val aus = Ausencia(
                        disciplinaId = disciplinaId.toLong(),
                        data = data,
                        justificativa = justificativa.takeIf { it.isNotBlank() },
                        categoria = categoria.takeIf { it.isNotBlank() }
                    )
                    viewModel.criarAusencia(aus)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5AB9D6))
            ) { Text("Salvar", color = Color.Black) }
            erro?.let { Text(text = it, color = Color.Red) }
        }
    }
}