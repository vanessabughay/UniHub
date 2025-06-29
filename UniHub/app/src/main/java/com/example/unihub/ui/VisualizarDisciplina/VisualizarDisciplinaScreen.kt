package com.example.unihub.ui.VisualizarDisciplina

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Para ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Para viewModel()
import com.example.unihub.components.CabecalhoAlternativo
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.model.HorarioAula // Se necessário para o preview
import com.example.unihub.data.remote.DisciplinaApiService // Para o Preview
import com.example.unihub.data.remote.RetrofitClient // Para o Preview
import com.example.unihub.data.repository.DisciplinaRepository // Para o Preview
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

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun VisualizarDisciplinaScreen(
    disciplinaId: String?,
    onVoltar: () -> Unit,
    onNavigateToEdit: (String) -> Unit, // Ação para navegar para a edição
    viewModel: VisualizarDisciplinaViewModel = viewModel() // Injeção do ViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState() // Coleta o estado do ViewModel

    // Efeito para carregar a disciplina quando a tela é iniciada ou o ID muda
    LaunchedEffect(key1 = disciplinaId) {
        viewModel.loadDisciplina(disciplinaId)
    }

    // Efeito para exibir mensagens de erro
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage() // Limpa a mensagem após exibir
        }
    }

    val disciplina = uiState.disciplina // A disciplina agora vem do ViewModel

    Scaffold(
        topBar = {
            CabecalhoAlternativo(
                titulo = disciplina?.nome ?: "Carregando...", // Exibe nome ou "Carregando..."
                onVoltar = onVoltar
                // Seu CabecalhoAlternativo atual não tem ícone à direita opcional, então não passamos nada aqui
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (disciplina == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.errorMessage ?: "Disciplina não encontrada ou erro ao carregar.")
            }
        } else {
            // Se a disciplina foi carregada com sucesso
            val opcoes = listOf(
                OpcaoDisciplina("Informações da disciplina", Icons.Outlined.Info, Color(0xFFD7EFF5)) {
                    onNavigateToEdit(disciplina.id) // Usa o ID da disciplina carregada
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
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Preview(showBackground = true)
@Composable
fun VisualizarDisciplinaScreenPreview() {
    MaterialTheme {
        val mockRepository = DisciplinaRepository(object : DisciplinaApiService {
            override suspend fun getDisciplinasResumoApi(): retrofit2.Response<List<com.example.unihub.data.remote.DisciplinaResumo>> =
                retrofit2.Response.success(emptyList())

            override suspend fun getDisciplinaByIdApi(id: String): retrofit2.Response<Disciplina> {
                return if (id == "id_preview") {
                    retrofit2.Response.success(
                        Disciplina(
                            id = "id_preview",
                            nome = "Projeto Integrador",
                            professor = "Prof. Ana Souza",
                            periodo = "2025/1",
                            cargaHoraria = 80,
                            aulas = listOf(
                                HorarioAula("Terça-feira", "Sala C10", "14:00", "18:00")
                            ),
                            dataInicioSemestre = LocalDate.of(2025, 2, 1),
                            dataFimSemestre = LocalDate.of(2025, 6, 30),
                            emailProfessor = "ana.souza@unihub.edu",
                            plataforma = "Moodle",
                            telefoneProfessor = "(11) 98765-4321",
                            salaProfessor = "Online",
                            isAtiva = true,
                            receberNotificacoes = true
                        )
                    )
                } else {
                    retrofit2.Response.success(null)
                }
            }

            override suspend fun addDisciplinaApi(disciplina: Disciplina): retrofit2.Response<Disciplina> = retrofit2.Response.success(disciplina)
            override suspend fun updateDisciplinaApi(id: String, disciplina: Disciplina): retrofit2.Response<Disciplina> = retrofit2.Response.success(disciplina)
            override suspend fun deleteDisciplinaApi(id: String): retrofit2.Response<Unit> = retrofit2.Response.success(Unit)
        })

        val mockViewModel = VisualizarDisciplinaViewModel(mockRepository)

        VisualizarDisciplinaScreen(
            disciplinaId = "id_preview", // Passe um ID para que o preview carregue dados
            onVoltar = {},
            onNavigateToEdit = {},
            viewModel = mockViewModel
        )
    }
}