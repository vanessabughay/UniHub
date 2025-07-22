package com.example.unihub

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresExtension
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
import com.example.unihub.ui.ManterAusencia.ManterAusenciaScreen

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

    object ManterAusencia : Screen("manter_ausencia/{disciplinaId}") {
        fun createRoute(disciplinaId: String): String {
            return "manter_ausencia/$disciplinaId"
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

                        val repository = com.example.unihub.data.repository.DisciplinaRepository(
                            com.example.unihub.data.repository.ApiDisciplinaBackend()
                        )
                        val factory = com.example.unihub.ui.ManterDisciplina.ManterDisciplinaViewModelFactory(repository)
                        val viewModel: com.example.unihub.ui.ManterDisciplina.ManterDisciplinaViewModel =
                            androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)

                        com.example.unihub.ui.ManterDisciplina.ManterDisciplinaScreen(
                            disciplinaId = disciplinaId,
                            onVoltar = { navController.popBackStack() },
                            onExcluirSucesso = {
                                navController.popBackStack(Screen.ListarDisciplinas.route, false)
                            },
                            viewModel = viewModel
                        )
                    }


                    // ROTA 3: Tela de Visualizar
                    composable(
                        route = Screen.VisualizarDisciplina.route,
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val disciplinaId = backStackEntry.arguments?.getString("id")

                        // Criação do ViewModel diretamente, sem remember (fora do escopo composable válido)
                        val disciplinaRepository = com.example.unihub.data.repository.DisciplinaRepository(
                            com.example.unihub.data.repository.ApiDisciplinaBackend(),
                        )
                        val ausenciaRepository = com.example.unihub.data.repository.AusenciaRepository(
                            com.example.unihub.data.repository.ApiAusenciaBackend(),
                        )
                        val factory = com.example.unihub.ui.VisualizarDisciplina.VisualizarDisciplinaViewModelFactory(
                            disciplinaRepository,
                            ausenciaRepository
                        )

                        val viewModel: com.example.unihub.ui.VisualizarDisciplina.VisualizarDisciplinaViewModel =
                            androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)

                        VisualizarDisciplinaScreen(
                            disciplinaId = disciplinaId,
                            onVoltar = { navController.popBackStack() },
                            onNavigateToEdit = { idDaDisciplinaParaEditar ->
                                navController.navigate(Screen.ManterDisciplina.createRoute(idDaDisciplinaParaEditar))
                            },

                            onNavigateToAusencias = { discId ->
                                navController.navigate(Screen.ManterAusencia.createRoute(discId))
                            },
                            viewModel = viewModel
                        )
                    }

                    // ROTA 4: Tela de Ausência
                    composable(
                        route = Screen.ManterAusencia.route,
                        arguments = listOf(navArgument("disciplinaId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val disciplinaIdArg = backStackEntry.arguments?.getString("disciplinaId") ?: ""

                        val ausenciaRepository = com.example.unihub.data.repository.AusenciaRepository(
                            com.example.unihub.data.repository.ApiAusenciaBackend(),
                        )
                        val disciplinaRepository = com.example.unihub.data.repository.DisciplinaRepository(
                            com.example.unihub.data.repository.ApiDisciplinaBackend(),
                        )
                        val categoriaRepository = com.example.unihub.data.repository.CategoriaRepository()
                        val factory = com.example.unihub.ui.ManterAusencia.ManterAusenciaViewModelFactory(
                            ausenciaRepository,
                            disciplinaRepository,
                            categoriaRepository
                        )
                        val viewModel: com.example.unihub.ui.ManterAusencia.ManterAusenciaViewModel =
                            androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)

                        ManterAusenciaScreen(
                            disciplinaId = disciplinaIdArg,
                            onVoltar = { navController.popBackStack() },
                            viewModel = viewModel
                        )
                    }

                }
            }
        }
    }
}