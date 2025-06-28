package com.example.unihub.ui.VisualizarDisciplina

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.model.HorarioAula
import java.time.LocalDate

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
        colors = ButtonDefaults.buttonColors(containerColor = item.background, contentColor = Color.Black),
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
fun VisualizarDisciplinaScreen(
    disciplinaId: String?,
    onVoltar: () -> Unit,
    onNavigateToEdit: (String) -> Unit // Ação para navegar para a edição
) {
    val context = LocalContext.current

    // Simulação de busca de dados
    val disciplina = remember(disciplinaId) {
        // TODO: Substituir pela busca de dados reais
        Disciplina(
            id = disciplinaId ?: "id_padrao_se_nulo",
            nome = "Desenvolvimento Web II",
            professor = "Prof. Ricardo Alves",
            periodo = "2025/1",
            cargaHoraria = 60, // int
            aulas = listOf(
                HorarioAula(
                    diaDaSemana = "Segunda-feira",
                    sala = "Lab Redes",
                    horarioInicio = "19:00",
                    horarioFim = "22:00"
                )
            ),
            dataInicioSemestre = LocalDate.of(2025, 2, 18),
            dataFimSemestre = LocalDate.of(2025, 6, 28),
            emailProfessor = "ricardo.alves@unihub.edu",
            plataforma = "GitHub, VS Code, Discord",
            telefoneProfessor = "(41) 99999-0303",
            salaProfessor = "LabRedes-01",
            isAtiva = true,
            receberNotificacoes = true
        )
    }

    val opcoes = listOf(
        OpcaoDisciplina("Informações da disciplina", Icons.Outlined.Info, Color(0xFFD7EFF5)) {
            // Ao clicar aqui, navega para a tela de edição passando o ID
            onNavigateToEdit(disciplina.id)
        },
        OpcaoDisciplina("Ausências", Icons.Outlined.CalendarToday, Color(0xFFF3E4F8)) {
            Toast.makeText(context, "Abrir Ausências", Toast.LENGTH_SHORT).show()
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
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(opcoes) { opcao ->
                BotaoOpcao(item = opcao)
            }
        }
    }
}