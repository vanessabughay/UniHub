package com.example.unihub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.unihub.ui.ListarDisciplinas.ListarDisciplinasScreen
import com.example.unihub.ui.ManterDisciplina.ManterDisciplinaScreen
import com.example.unihub.ui.VisualizarDisciplina.VisualizarDisciplinaScreen

// Definição das telas e suas rotas
sealed class Screen(val route: String) {
    object ListarDisciplinas : Screen("lista_disciplinas")

    object ManterDisciplina : Screen("manter_disciplina?id={id}") {
        // Função para criar a rota de "manter", com ou sem ID
        fun createRoute(id: String?): String {
            return if (id != null) "manter_disciplina?id=$id" else "manter_disciplina"
        }
    }

    object VisualizarDisciplina : Screen("visualizar_disciplina/{id}") {
        // Função para criar a rota de "visualizar", que sempre exige um ID
        fun createRoute(id: String): String {
            return "visualizar_disciplina/$id"
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                // Configuração da Navegação
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.ListarDisciplinas.route
                ) {
                    // ROTA 1: Tela de Listar
                    composable(Screen.ListarDisciplinas.route) {
                        ListarDisciplinasScreen(
                            onAddDisciplina = {
                                navController.navigate(Screen.ManterDisciplina.createRoute(null))
                            },
                            onDisciplinaDoubleClick = { disciplinaId ->
                                navController.navigate(Screen.VisualizarDisciplina.createRoute(disciplinaId))
                            }
                        )
                    }

                    // ROTA 2: Tela de Manter (Criar/Editar)
                    composable(
                        route = Screen.ManterDisciplina.route,
                        arguments = listOf(navArgument("id") {
                            type = NavType.StringType
                            nullable = true
                        })
                    ) { backStackEntry ->
                        val disciplinaId = backStackEntry.arguments?.getString("id")
                        ManterDisciplinaScreen(
                            disciplinaId = disciplinaId,
                            onVoltar = { navController.popBackStack() }
                        )
                    }

                    // ROTA 3: Tela de Visualizar
                    composable(
                        route = Screen.VisualizarDisciplina.route,
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val disciplinaId = backStackEntry.arguments?.getString("id")

                        // A chamada para a tela agora inclui a lógica de navegação para edição
                        VisualizarDisciplinaScreen(
                            disciplinaId = disciplinaId,
                            onVoltar = { navController.popBackStack() },
                            onNavigateToEdit = { idDaDisciplinaParaEditar ->
                                // Navega para a tela Manter, passando o ID para o modo de edição
                                navController.navigate(Screen.ManterDisciplina.createRoute(idDaDisciplinaParaEditar))
                            }
                        )
                    }
                }
            }
        }
    }
}