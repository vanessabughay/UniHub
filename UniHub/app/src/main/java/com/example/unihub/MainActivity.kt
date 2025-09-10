package com.example.unihub

import android.os.Build
import android.os.Bundle
import android.net.Uri
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
import com.example.unihub.ui.ListarContato.ListarContatoScreen

import com.example.unihub.ui.ManterConta.ManterContaScreen
import com.example.unihub.ui.ManterDisciplina.ManterDisciplinaScreen
import com.example.unihub.ui.VisualizarDisciplina.VisualizarDisciplinaScreen
import com.example.unihub.ui.ManterInstituicao.ManterInstituicaoScreen
import com.example.unihub.ui.ManterAusencia.ManterAusenciaScreen
import com.example.unihub.data.repository.ApiCategoriaBackend
import com.example.unihub.ui.ListarGrupo.ListarGrupoScreen
import com.example.unihub.ui.ManterContato.ManterContatoScreen
import com.example.unihub.ui.ManterGrupo.ManterGrupoScreen
import com.example.unihub.ui.TelaInicial.TelaInicial
import com.example.unihub.ui.login.LoginScreen
import com.example.unihub.ui.register.RegisterScreen
import com.example.unihub.data.api.TokenManager

// Definição das telas e suas rotas
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object TelaInicial : Screen("tela_inicial")
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

    object Anotacoes : Screen("anotacoes/{id}") {
        fun createRoute(id: String): String {
            return "anotacoes/$id"
        }
    }

    object ManterAusencia : Screen("manter_ausencia?disciplinaId={disciplinaId}&id={id}") {
        fun createRoute(disciplinaId: String, id: String? = null): String {
            return buildString {
                append("manter_ausencia?disciplinaId=$disciplinaId")
                if (id != null) append("&id=$id")
            }
        }
    }
    object ManterConta : Screen("manter_conta")
    object ManterInstituicao :
        Screen("manter_instituicao?nome={nome}&media={media}&frequencia={frequencia}") {
        fun createRoute(nome: String, media: String, frequencia: String): String {
            return "manter_instituicao?nome=${Uri.encode(nome)}&media=${Uri.encode(media)}&frequencia=${Uri.encode(frequencia)}"
        }
    }

    object ListarContato : Screen("lista_contato") // Rota é "lista_contato"
    object ManterContato : Screen("manter_contato?id={id}") {
        fun createRoute(id: String?): String {
            return if (id != null) "manter_contato?id=$id" else "manter_contato"
        }
    }
    object ListarGrupo : Screen("lista_grupo")
    object ManterGrupo : Screen("manter_grupo?id={id}"){
        fun createRoute(id: String?): String {
            return if (id != null) "manter_grupo?id=$id" else "manter_grupo"
        }
    }
}

class MainActivity : ComponentActivity() {
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenManager.loadToken(applicationContext)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                // Configuração da Navegação
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Login.route
                ) {
                    // ROTA LOGIN: Tela de Login
                    composable(Screen.Login.route) {
                        LoginScreen(navController = navController)
                    }

                    // ROTA REGISTER: Tela de Registro
                    composable(Screen.Register.route) {
                        RegisterScreen(navController = navController)
                    }

                    // ROTA 1: Tela de Listar
                    composable(Screen.ListarDisciplinas.route) {
                        ListarDisciplinasScreen(
                            onAddDisciplina = {
                                navController.navigate(Screen.ManterDisciplina.createRoute(null))
                            },

                            onDisciplinaDoubleClick = { disciplinaId ->
                                navController.navigate(Screen.VisualizarDisciplina.createRoute(disciplinaId))},

                            onVoltar = { navController.popBackStack()}

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

                            onNavigateToAusencias = { discId, ausId ->
                                navController.navigate(Screen.ManterAusencia.createRoute(discId, ausId))
                            },
                            onNavigateToAnotacoes = { idDaDisciplina ->
                                navController.navigate(Screen.Anotacoes.createRoute(idDaDisciplina))
                            },

                            viewModel = viewModel
                        )
                    }

                    // ROTA 4: Tela de Ausência
                    composable(
                        route = Screen.ManterAusencia.route,
                        arguments = listOf(
                            navArgument("disciplinaId") { type = NavType.StringType },
                            navArgument("id") { type = NavType.StringType; nullable = true }
                        )
                    ) { backStackEntry ->
                        val disciplinaIdArg = backStackEntry.arguments?.getString("disciplinaId") ?: ""
                        val ausenciaIdArg = backStackEntry.arguments?.getString("id")

                        val ausenciaRepository = com.example.unihub.data.repository.AusenciaRepository(
                            com.example.unihub.data.repository.ApiAusenciaBackend(),
                        )
                        val disciplinaRepository = com.example.unihub.data.repository.DisciplinaRepository(
                            com.example.unihub.data.repository.ApiDisciplinaBackend(),
                        )
                        val categoriaRepository = com.example.unihub.data.repository.CategoriaRepository(
                            ApiCategoriaBackend(),
                        )
                        val factory = com.example.unihub.ui.ManterAusencia.ManterAusenciaViewModelFactory(
                            ausenciaRepository,
                            disciplinaRepository,
                            categoriaRepository
                        )
                        val viewModel: com.example.unihub.ui.ManterAusencia.ManterAusenciaViewModel =
                            androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)

                        ManterAusenciaScreen(
                            disciplinaId = disciplinaIdArg,
                            ausenciaId = ausenciaIdArg,
                            onVoltar = { navController.popBackStack() },
                            viewModel = viewModel
                        )
                    }
                    // ROTA 5: Tela de Manter Conta
                    composable(Screen.ManterConta.route) {
                        ManterContaScreen(
                            onVoltar = { navController.popBackStack() },
                            onNavigateToManterInstituicao = { nome, media, frequencia ->
                                navController.navigate(
                                    Screen.ManterInstituicao.createRoute(nome, media, frequencia)
                                )
                            }
                        )
                    }
                    // ROTA 6: Tela de Manter Instituição
                    composable(
                        route = Screen.ManterInstituicao.route,
                        arguments = listOf(
                            navArgument("nome") { type = NavType.StringType; defaultValue = "" },
                            navArgument("media") { type = NavType.StringType; defaultValue = "" },
                            navArgument("frequencia") { type = NavType.StringType; defaultValue = "" }
                        )
                    ) { backStackEntry ->
                        val nomeArg = backStackEntry.arguments?.getString("nome") ?: ""
                        val mediaArg = backStackEntry.arguments?.getString("media") ?: ""
                        val frequenciaArg = backStackEntry.arguments?.getString("frequencia") ?: ""

                        ManterInstituicaoScreen(
                            onVoltar = { navController.popBackStack() },
                            nome = nomeArg,
                            media = mediaArg,
                            frequencia = frequenciaArg
                        )
                    }


                    // ROTA 7: Tela de Listar Contatos
                    composable(Screen.ListarContato.route) { // Usa a rota "lista_contato"
                        ListarContatoScreen(
                            onAddContato = {
                                navController.navigate(Screen.ManterContato.createRoute(null))
                            },
                            onContatoClick = { contatoId ->
                                navController.navigate(Screen.ManterContato.createRoute(contatoId))
                            },
                            onVoltar = { navController.popBackStack() }

                        )
                    }

                    // ROTA 8: manter Contatos
                    composable(
                        route = Screen.ManterContato.route,
                        arguments = listOf(navArgument("id") {
                            type = NavType.StringType
                            nullable = true
                        })
                    ) { backStackEntry ->
                        val contatoId = backStackEntry.arguments?.getString("id")
                        ManterContatoScreen(
                            contatoId = contatoId,
                            onVoltar = { navController.popBackStack() },
                            onExcluirSucessoNavegarParaLista = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // ROTA 9: Tela de Listar Grupo
                    composable(Screen.ListarGrupo.route) {
                        ListarGrupoScreen(
                            onAddGrupo = {
                                navController.navigate(Screen.ManterGrupo.createRoute(null)) // Para novo grupo
                            },
                            // onNavigateToManterGrupo é usado PELO DIÁLOGO para a ação de EDITAR
                            onNavigateToManterGrupo = { grupoId -> // Este grupoId virá do diálogo de detalhes
                                navController.navigate(Screen.ManterGrupo.createRoute(grupoId))
                            },
                            onVoltar = { navController.popBackStack() }
                        )
                    }

                    // ROTA 10: manter Grupo
                    composable(
                        route = Screen.ManterGrupo.route,
                        arguments = listOf(navArgument("id") {
                            type = NavType.StringType
                            nullable = true
                        })
                    ) { backStackEntry ->
                        val grupoId = backStackEntry.arguments?.getString("id")
                        ManterGrupoScreen(
                            grupoId = grupoId,
                            onVoltar = { navController.popBackStack() },
                            onExcluirSucessoNavegarParaLista = {
                                navController.popBackStack() // Volta para a lista após exclusão
                            }
                        )
                    }



                    //TELA INICIAL

                    composable(Screen.TelaInicial.route) {
                        TelaInicial(navController = navController)
                    }

                    // Anotações
                    composable(
                        route = Screen.Anotacoes.route,
                        arguments = listOf(navArgument("id") { type = NavType.LongType }) // Use LongType para IDs
                    ) { backStackEntry ->
                        val disciplinaId = backStackEntry.arguments?.getLong("id")
                        if (disciplinaId != null) {
                            // Importe a AnotacoesView
                            com.example.unihub.ui.Anotacoes.AnotacoesView(
                                disciplinaId = disciplinaId,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }


                }
            }
        }
    }
}