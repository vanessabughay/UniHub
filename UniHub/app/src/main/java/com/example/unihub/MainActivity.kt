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

// Definição das telas e suas rotas
sealed class Screen(val route: String) {
    object ListarDisciplinas : Screen("lista_disciplinas")

    // Rota única para Manter Disciplina (Criar, Visualizar e Editar)
    // id=null -> Criar
    // id=existente -> Visualizar/Editar (a tela ManterDisciplinaScreen decidirá o modo)
    object ManterDisciplina : Screen("manter_disciplina?id={id}") {
        fun createRoute(id: String? = null): String {
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
                                navController.navigate(Screen.ManterDisciplina.createRoute(null))
                            },
                            onDisciplinaDoubleClick = { disciplinaId ->
                                navController.navigate(Screen.ManterDisciplina.createRoute(disciplinaId))
                            }
                        )
                    }

                    // ROTA 2: Tela de Manter Disciplina (Criar/Visualizar/Editar)
                    composable(
                        route = Screen.ManterDisciplina.route,
                        arguments = listOf(navArgument("id") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        })
                    ) { backStackEntry ->
                        val disciplinaId = backStackEntry.arguments?.getString("id")

                        ManterDisciplinaScreen( // Referencia a tela do pacote corrigido
                            disciplinaId = disciplinaId,
                            onBack = { navController.popBackStack() },
                            onSaveSuccess = {
                                // Volta para a tela de listagem e limpa a pilha acima dela
                                // Isso garante que a lista pode ser recarregada ao voltar
                                navController.popBackStack(Screen.ListarDisciplinas.route, inclusive = false)
                            },
                            onDeleteSuccess = {
                                // Volta para a tela de listagem após a exclusão
                                navController.popBackStack(Screen.ListarDisciplinas.route, inclusive = false)
                            }
                        )
                    }
                }
            }
        }
    }
}