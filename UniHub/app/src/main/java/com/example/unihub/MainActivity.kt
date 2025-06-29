package com.example.unihub

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.unihub.ui.ListarDisciplinas.ListarDisciplinasScreen
import com.example.unihub.ui.ManterDisciplina.ManterDisciplinaScreen
import com.example.unihub.ui.VisualizarDisciplina.VisualizarDisciplinaScreen // <<< IMPORT CORRETO para a Tela de Detalhes (VisualizarDisciplina)

// Definição das telas e suas rotas
sealed class Screen(val route: String) {
    object ListarDisciplinas : Screen("lista_disciplinas")

    // Rota para Visualizar (detalhes e navegação para edição)
    // Note que o ID é OBRIGATÓRIO aqui e do tipo Long
    object VisualizarDisciplina : Screen("visualizar_disciplina/{id}") {
        fun createRoute(id: Long): String { // <<< MUDANÇA: id para Long
            return "visualizar_disciplina/$id"
        }
    }

    // Rota para Manter (Criar e Editar - é o FORMULÁRIO)
    // O ID é OPCIONAL, pois pode ser uma nova disciplina
    object ManterDisciplina : Screen("manter_disciplina?id={id}") {
        fun createRoute(id: Long? = null): String { // <<< MUDANÇA: id para Long?
            // Converte Long? para String para a rota, mas o NavType saberá converter de volta
            return if (id != null) "manter_disciplina?id=$id" else "manter_disciplina"
        }
    }
}

class MainActivity : ComponentActivity() {
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.ListarDisciplinas.route
                ) {
                    // ROTA 1: Tela de Listar Disciplinas
                    composable(Screen.ListarDisciplinas.route) {
                        ListarDisciplinasScreen(
                            onAddDisciplina = {
                                // Navega para a tela ManterDisciplina (formulário) no modo de criação (ID nulo)
                                navController.navigate(Screen.ManterDisciplina.createRoute(null))
                            },
                            onDisciplinaDoubleClick = { disciplinaId ->
                                // Navega para a tela VisualizarDisciplina (detalhes), passando o ID Long
                                navController.navigate(Screen.VisualizarDisciplina.createRoute(disciplinaId)) // <<< MUDANÇA: id para Long
                            }
                        )
                    }

                    // ROTA 2: Tela de Visualizar Disciplina (Detalhes de uma disciplina específica)
                    composable(
                        route = Screen.VisualizarDisciplina.route,
                        // O ID aqui é OBRIGATÓRIO e do tipo Long
                        arguments = listOf(navArgument("id") { type = NavType.LongType }) // <<< MUDANÇA: Type para LongType
                    ) { backStackEntry ->
                        // Recebe o ID como Long
                        val disciplinaId = backStackEntry.arguments?.getLong("id")

                        VisualizarDisciplinaScreen(
                            disciplinaId = disciplinaId, // Passa o Long? para o ViewModel
                            onVoltar = { navController.popBackStack() },
                            onNavigateToEdit = { idDaDisciplinaParaEditar ->
                                // Navega da tela Visualizar para a tela Manter (formulário) no modo de edição
                                navController.navigate(Screen.ManterDisciplina.createRoute(idDaDisciplinaParaEditar)) // <<< MUDANÇA: id para Long
                            }
                        )
                    }

                    // ROTA 3: Tela de Manter Disciplina (Criar/Editar - FORMULÁRIO)
                    composable(
                        route = Screen.ManterDisciplina.route,
                        // O ID aqui é OPCIONAL e do tipo Long
                        arguments = listOf(navArgument("id") {
                            type = NavType.LongType // <<< MUDANÇA: Type para LongType
                            nullable = true
                            defaultValue = null // Garante que o ID é nulo se não for passado
                        })
                    ) { backStackEntry ->
                        // Recebe o ID como Long?
                        val disciplinaId = backStackEntry.arguments?.getLong("id")

                        ManterDisciplinaScreen( // Este é o seu formulário de Manter
                            disciplinaId = disciplinaId, // Passa o Long? para o ViewModel
                            onBack = { navController.popBackStack() },
                            onSaveSuccess = {
                                // Após salvar com sucesso, volta para a tela de listagem
                                navController.popBackStack(Screen.ListarDisciplinas.route, inclusive = false)
                            },
                            onDeleteSuccess = {
                                // Após deletar com sucesso, volta para a tela de listagem
                                navController.popBackStack(Screen.ListarDisciplinas.route, inclusive = false)
                            }
                        )
                    }
                }
            }
        }
    }
}