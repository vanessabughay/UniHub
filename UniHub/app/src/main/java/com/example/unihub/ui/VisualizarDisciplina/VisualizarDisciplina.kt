package com.example.unihub.ui.VisualizarDisciplina

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.model.Ausencia

data class OpcaoDisciplina(
    val title: String,
    val icon: ImageVector,
    val background: Color,
    val onClick: () -> Unit
)

@Composable
fun BotaoOpcao(item: OpcaoDisciplina) {
    Button(
        onClick = item.onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = item.background,
            contentColor = Color.Black
        ),
        contentPadding = PaddingValues(vertical = 20.dp, horizontal = 16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(imageVector = item.icon, contentDescription = null, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = item.title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun AusenciasCard(
    expanded: Boolean,
    ausencias: List<Ausencia>,
    onToggle: () -> Unit,
    onAdd: () -> Unit,
    onItemClick: (Ausencia) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E4F8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Ausências", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Ausência")
                }
            }
            if (expanded) {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                if (ausencias.isEmpty()) {
                    Text(
                        text = "Nenhuma ausência registrada",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else {
                    ausencias.forEach { aus ->
                        Text(
                            text = aus.data.format(formatter) + (aus.categoria?.let { " - $it" } ?: ""),
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { onItemClick(aus) }
                        )
                    }
                }
            }
        }
    }
}


@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun VisualizarDisciplinaScreen(
    disciplinaId: String?,
    onVoltar: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToAusencias: (String, String?) -> Unit,
    viewModel: VisualizarDisciplinaViewModel
) {
    val context = LocalContext.current
    val disciplina by viewModel.disciplina.collectAsStateWithLifecycle()
    val erro by viewModel.erro.collectAsStateWithLifecycle()
    val ausencias by viewModel.ausencias.collectAsStateWithLifecycle()
    var expandAusencias by remember { mutableStateOf(false) }

    LaunchedEffect(disciplinaId) {
        disciplinaId?.let { viewModel.loadDisciplina(it) }
    }

    erro?.let {
        Toast.makeText(context, "Erro: $it", Toast.LENGTH_LONG).show()
    }

    disciplina?.let { disciplina ->
        val opcoes = listOf(
            OpcaoDisciplina("Informações da disciplina", Icons.Outlined.Info, Color(0xFFD7EFF5)) {
                onNavigateToEdit(disciplina.id.toString())

            },

            OpcaoDisciplina("Notas", Icons.Outlined.StarOutline, Color(0xFFE0E1F8)) {
                Toast.makeText(context, "Abrir Notas", Toast.LENGTH_SHORT).show()
            },
            OpcaoDisciplina("Minhas anotações", Icons.Outlined.Notes, Color(0xFFF8F1E1)) {
                Toast.makeText(context, "Abrir Anotações", Toast.LENGTH_SHORT).show()
            },
            OpcaoDisciplina("Arquivos", Icons.Outlined.Download, Color(0xFFE6F7EC)) {
                Toast.makeText(context, "Abrir Arquivos", Toast.LENGTH_SHORT).show()
            }
        )

        Scaffold(
            topBar = {
                CabecalhoAlternativo(
                    titulo = disciplina.nome,
                    onVoltar = onVoltar
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    BotaoOpcao(item = opcoes.first())
                }
                item {
                    AusenciasCard(
                        expanded = expandAusencias,
                        ausencias = ausencias,
                        onToggle = { expandAusencias = !expandAusencias },
                        onAdd = { onNavigateToAusencias(disciplina.id.toString(), null) },
                        onItemClick = { aus ->
                            onNavigateToAusencias(disciplina.id.toString(), aus.id?.toString())
                        }
                    )
                }
                items(opcoes.drop(1)) { opcao ->
                    BotaoOpcao(item = opcao)
                }
            }
        }
    }
}
