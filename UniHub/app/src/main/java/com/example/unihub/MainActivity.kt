package com.example.unihub

import android.os.Build
import android.os.Bundle
<<<<<<< Updated upstream
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
=======
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.ui.ListarDisciplinas.ListarDisciplinasScreen
import com.example.unihub.ui.ManterDisciplina.ManterDisciplinaScreen
import com.example.unihub.ui.ManterDisciplina.ManterDisciplinaViewModel
import com.example.unihub.ui.ManterDisciplina.ManterDisciplinaViewModelFactory
import com.example.unihub.ui.VisualizarDisciplina.VisualizarDisciplinaScreen

sealed class Screen(val route: String) {
    object ListarDisciplinas : Screen("lista_disciplinas")

    object ManterDisciplina : Screen("manter_disciplina?id={id}") {
        fun createRoute(id: Long?): String {
            return if (id != null) "manter_disciplina?id=$id" else "manter_disciplina"
        }
    }

    object VisualizarDisciplina : Screen("visualizar_disciplina/{id}") {
        fun createRoute(id: Long): String {
            return "visualizar_disciplina/$id"
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
                    // Tela 1: Listar Disciplinas
                    composable(Screen.ListarDisciplinas.route) {
                        ListarDisciplinasScreen(
                            onAddDisciplina = {
                                navController.navigate(Screen.ManterDisciplina.createRoute(null))
                            },
                            onDisciplinaDoubleClick = { disciplinaIdLong ->
                                navController.navigate(Screen.VisualizarDisciplina.createRoute(disciplinaIdLong))
                            }
                        )
                    }

                    // Tela 2: Criar ou Editar Disciplina
                    composable(
                        route = Screen.ManterDisciplina.route,
                        arguments = listOf(navArgument("id") {
                            type = NavType.LongType
                            nullable = true
                            defaultValue = null
                        })
                    ) { backStackEntry ->
                        val disciplinaIdLong = backStackEntry.arguments?.getLong("id")

                        ManterDisciplinaScreen(
                            disciplinaId = disciplinaIdLong,
                            onVoltar = { navController.popBackStack() }
                        )
                    }


                    // Tela 3: Visualizar Disciplina
                    composable(
                        route = Screen.VisualizarDisciplina.route,
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val disciplinaIdLong = backStackEntry.arguments?.getLong("id")

                        VisualizarDisciplinaScreen(
                            disciplinaId = disciplinaIdLong,
                            onVoltar = { navController.popBackStack() },
                            onNavigateToEdit = { idParaEditar ->
                                navController.navigate(Screen.ManterDisciplina.createRoute(idParaEditar))
                            }
                        )
                    }
                }
            }
>>>>>>> Stashed changes
        }
    }
}
