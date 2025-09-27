package com.example.unihub

import android.os.Bundle
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.unihub.data.apiBackend.ApiAusenciaBackend
import android.content.Intent


import com.example.unihub.ui.ListarDisciplinas.ListarDisciplinasScreen
import com.example.unihub.ui.ListarContato.ListarContatoScreen
import com.example.unihub.ui.ManterConta.ManterContaScreen
import com.example.unihub.ui.ManterDisciplina.ManterDisciplinaScreen
import com.example.unihub.ui.VisualizarDisciplina.VisualizarDisciplinaScreen
import com.example.unihub.ui.ManterInstituicao.ManterInstituicaoScreen
import com.example.unihub.ui.ManterAusencia.ManterAusenciaScreen
import com.example.unihub.data.apiBackend.ApiCategoriaBackend
import com.example.unihub.data.apiBackend.ApiDisciplinaBackend
import com.example.unihub.ui.ListarGrupo.ListarGrupoScreen
import com.example.unihub.ui.ManterContato.ManterContatoScreen
import com.example.unihub.ui.ManterGrupo.ManterGrupoScreen
import com.example.unihub.ui.ListarAvaliacao.ListarAvaliacaoScreen
import com.example.unihub.ui.ManterAvaliacao.ManterAvaliacaoScreen
import com.example.unihub.ui.TelaInicial.TelaInicial
import com.example.unihub.ui.Login.LoginScreen
import com.example.unihub.ui.Registro.RegisterScreen
import com.example.unihub.data.config.TokenManager
import com.example.unihub.ui.ListarQuadros.ListarQuadrosScreen
import com.example.unihub.ui.ListarQuadros.ListarQuadrosViewModelFactory
import com.example.unihub.data.apiBackend.ApiQuadroBackend
import com.example.unihub.data.repository.QuadroRepository
import com.example.unihub.ui.ManterQuadro.QuadroFormScreen
import com.example.unihub.ui.ManterQuadro.QuadroFormViewModelFactory
import com.example.unihub.ui.TelaEsqueciSenha.TelaEsqueciSenha
import com.example.unihub.ui.TelaEsqueciSenha.TelaRedefinirSenha

// Definição das telas e suas rotas
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object TelaInicial : Screen("tela_inicial")

    object ListarDisciplinas : Screen("lista_disciplinas")

    object ListarQuadros : Screen("lista_quadros")
    object ManterQuadro : Screen("quadroForm/{quadroId}") {
        fun createRoute(quadroId: String = "new") = "quadroForm/$quadroId"
    }

    object ManterDisciplina : Screen("manter_disciplina?id={id}") {
        fun createRoute(id: String?) = if (id != null) "manter_disciplina?id=$id" else "manter_disciplina"
    }

    object VisualizarDisciplina : Screen("visualizar_disciplina/{id}") {
        fun createRoute(id: String) = "visualizar_disciplina/$id"
    }

    object Anotacoes : Screen("anotacoes/{id}") {
        fun createRoute(id: Long) = "anotacoes/$id"
    }

    object ManterAusencia : Screen("manter_ausencia?disciplinaId={disciplinaId}&id={id}") {
        fun createRoute(disciplinaId: String, id: String? = null): String =
            buildString {
                append("manter_ausencia?disciplinaId=$disciplinaId")
                if (id != null) append("&id=$id")
            }
    }

    object ManterConta : Screen("manter_conta")

    object ManterInstituicao :
        Screen("manter_instituicao?nome={nome}&media={media}&frequencia={frequencia}") {
        fun createRoute(nome: String, media: String, frequencia: String) =
            "manter_instituicao?nome=${Uri.encode(nome)}&media=${Uri.encode(media)}&frequencia=${Uri.encode(frequencia)}"
    }

    object ListarContato : Screen("lista_contato")
    object ManterContato : Screen("manter_contato?id={id}") {
        fun createRoute(id: String?) = if (id != null) "manter_contato?id=$id" else "manter_contato"
    }

    object ListarGrupo : Screen("lista_grupo")
    object ManterGrupo : Screen("manter_grupo?id={id}") {
        fun createRoute(id: String?) = if (id != null) "manter_grupo?id=$id" else "manter_grupo"
    }

    object EsqueciSenha : Screen("esqueci_senha")
    object RedefinirSenha : Screen("redefinir_senha?token={token}") {
        fun createRoute(token: String) = "redefinir_senha?token=$token"
    }

    // === AVAILIAÇÃO (ADICIONADAS) ===
    object ListarAvaliacao : Screen("lista_avaliacao")

    object ManterAvaliacao : Screen("manter_avaliacao?id={id}&disciplinaId={disciplinaId}") {
        const val ARG_ID = "id"
        const val ARG_DISC = "disciplinaId"

        fun createRoute(id: String?, disciplinaId: String?): String {
            val idPart = id ?: ""
            val discPart = disciplinaId ?: ""
            return "manter_avaliacao?$ARG_ID=$idPart&$ARG_DISC=$discPart"
        }
    }
}

class MainActivity : ComponentActivity() {

    private lateinit var navController: androidx.navigation.NavHostController


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenManager.loadToken(applicationContext)

        val startIntent = intent

        setContent {
            navController = rememberNavController()
            val startDest = if (TokenManager.token.isNullOrBlank())
                Screen.Login.route
            else
                Screen.TelaInicial.route

            LaunchedEffect(Unit) {
                navController.handleDeepLink(startIntent)
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                NavHost(
                    navController = navController,
                    startDestination = startDest
                ) {
                    // LOGIN
                    composable(Screen.Login.route) {
                        LoginScreen(navController = navController)
                    }

                    // REGISTER
                    composable(Screen.Register.route) {
                        RegisterScreen(navController = navController)
                    }

                    // LISTAR DISCIPLINAS
                    composable(Screen.ListarDisciplinas.route) {
                        ListarDisciplinasScreen(
                            onAddDisciplina = {
                                navController.navigate(Screen.ManterDisciplina.createRoute(null))
                            },
                            onDisciplinaDoubleClick = { disciplinaId ->
                                navController.navigate(
                                    Screen.VisualizarDisciplina.createRoute(
                                        disciplinaId
                                    )
                                )
                            },
                            onVoltar = { navController.popBackStack() }
                        )
                    }

                    // MANTER DISCIPLINA
                    composable(
                        route = Screen.ManterDisciplina.route,
                        arguments = listOf(navArgument("id") {
                            type = NavType.StringType
                            nullable = true
                        })
                    ) { backStackEntry ->
                        val disciplinaId = backStackEntry.arguments?.getString("id")
                        val repository = com.example.unihub.data.repository.DisciplinaRepository(
                            ApiDisciplinaBackend()
                        )
                        val factory =
                            com.example.unihub.ui.ManterDisciplina.ManterDisciplinaViewModelFactory(
                                repository
                            )
                        val viewModel: com.example.unihub.ui.ManterDisciplina.ManterDisciplinaViewModel =
                            androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)

                        ManterDisciplinaScreen(
                            disciplinaId = disciplinaId,
                            onVoltar = { navController.popBackStack() },
                            onExcluirSucesso = {
                                navController.popBackStack(
                                    Screen.ListarDisciplinas.route,
                                    inclusive = false
                                )
                            },
                            viewModel = viewModel
                        )
                    }

                    // VISUALIZAR DISCIPLINA
                    composable(
                        route = Screen.VisualizarDisciplina.route,
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val disciplinaId = backStackEntry.arguments?.getString("id")

                        val disciplinaRepository =
                            com.example.unihub.data.repository.DisciplinaRepository(
                                ApiDisciplinaBackend(),
                            )
                        val ausenciaRepository =
                            com.example.unihub.data.repository.AusenciaRepository(
                                ApiAusenciaBackend(),
                            )
                        val factory =
                            com.example.unihub.ui.VisualizarDisciplina.VisualizarDisciplinaViewModelFactory(
                                disciplinaRepository,
                                ausenciaRepository
                            )

                        val viewModel: com.example.unihub.ui.VisualizarDisciplina.VisualizarDisciplinaViewModel =
                            androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)

                        VisualizarDisciplinaScreen(
                            disciplinaId = disciplinaId,
                            onVoltar = { navController.popBackStack() },
                            onNavigateToEdit = { idParaEditar ->
                                navController.navigate(
                                    Screen.ManterDisciplina.createRoute(
                                        idParaEditar
                                    )
                                )
                            },
                            onNavigateToAusencias = { discId, ausId ->
                                navController.navigate(
                                    Screen.ManterAusencia.createRoute(
                                        discId,
                                        ausId
                                    )
                                )
                            },
                            onNavigateToAnotacoes = { idDaDisciplina ->
                                navController.navigate(Screen.Anotacoes.createRoute(idDaDisciplina.toLong()))
                            },
                            viewModel = viewModel
                        )
                    }

                    // MANTER AUSÊNCIA
                    composable(
                        route = Screen.ManterAusencia.route,
                        arguments = listOf(
                            navArgument("disciplinaId") { type = NavType.StringType },
                            navArgument("id") { type = NavType.StringType; nullable = true }
                        )
                    ) { backStackEntry ->
                        val disciplinaIdArg =
                            backStackEntry.arguments?.getString("disciplinaId") ?: ""
                        val ausenciaIdArg = backStackEntry.arguments?.getString("id")

                        val ausenciaRepository =
                            com.example.unihub.data.repository.AusenciaRepository(
                                ApiAusenciaBackend(),
                            )
                        val disciplinaRepository =
                            com.example.unihub.data.repository.DisciplinaRepository(
                                ApiDisciplinaBackend(),
                            )
                        val categoriaRepository =
                            com.example.unihub.data.repository.CategoriaRepository(
                                ApiCategoriaBackend(),
                            )
                        val factory =
                            com.example.unihub.ui.ManterAusencia.ManterAusenciaViewModelFactory(
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

                    // MANTER CONTA
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

                    // MANTER INSTITUIÇÃO
                    composable(
                        route = Screen.ManterInstituicao.route,
                        arguments = listOf(
                            navArgument("nome") { type = NavType.StringType; defaultValue = "" },
                            navArgument("media") { type = NavType.StringType; defaultValue = "" },
                            navArgument("frequencia") {
                                type = NavType.StringType; defaultValue = ""
                            }
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

                    // LISTAR CONTATO
                    composable(Screen.ListarContato.route) {
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

                    // MANTER CONTATO
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
                            onExcluirSucessoNavegarParaLista = { navController.popBackStack() }
                        )
                    }

                    // LISTAR GRUPO
                    composable(Screen.ListarGrupo.route) {
                        ListarGrupoScreen(
                            onAddGrupo = {
                                navController.navigate(Screen.ManterGrupo.createRoute(null))
                            },
                            onNavigateToManterGrupo = { grupoId ->
                                navController.navigate(Screen.ManterGrupo.createRoute(grupoId))
                            },
                            onVoltar = { navController.popBackStack() }
                        )
                    }

                    // MANTER GRUPO
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
                            onExcluirSucessoNavegarParaLista = { navController.popBackStack() }
                        )
                    }

                    // LISTAR QUADROS (FECHO CORRIGIDO)
                    composable(Screen.ListarQuadros.route) {
                        val quadroRepository = QuadroRepository(ApiQuadroBackend.apiService)
                        val viewModelFactory = ListarQuadrosViewModelFactory(quadroRepository)

                        ListarQuadrosScreen(
                            navController = navController,
                            viewModelFactory = viewModelFactory
                        )
                    }

                    // LISTAR AVALIAÇÃO (agora existe na sealed class)
                    composable(Screen.ListarAvaliacao.route) {
                        ListarAvaliacaoScreen(
                            onAddAvaliacaoGeral = {
                                navController.navigate(
                                    Screen.ManterAvaliacao.createRoute(
                                        id = null,
                                        disciplinaId = null
                                    )
                                )
                            },
                            onAddAvaliacaoParaDisciplina = { disciplinaId ->
                                navController.navigate(
                                    Screen.ManterAvaliacao.createRoute(
                                        id = null,
                                        disciplinaId = disciplinaId
                                    )
                                )
                            },
                            onNavigateToManterAvaliacao = { avaliacaoId ->
                                navController.navigate(
                                    Screen.ManterAvaliacao.createRoute(
                                        id = avaliacaoId,
                                        disciplinaId = null
                                    )
                                )
                            },
                            onVoltar = { navController.popBackStack() }
                        )
                    }

                    // MANTER QUADRO
                    composable(
                        route = Screen.ManterQuadro.route,
                        arguments = listOf(
                            navArgument("quadroId") {
                                type = NavType.StringType
                                defaultValue = "new"
                            }
                        )
                    ) { backStackEntry ->
                        val quadroIdArg = backStackEntry.arguments?.getString("quadroId")
                        val quadroId = quadroIdArg?.takeUnless { it == "new" }

                        val quadroRepository = QuadroRepository(ApiQuadroBackend.apiService)
                        val viewModelFactory = QuadroFormViewModelFactory(quadroRepository)

                        QuadroFormScreen(
                            navController = navController,
                            quadroId = quadroId,
                            viewModelFactory = viewModelFactory
                        )
                    }

                    // MANTER AVALIAÇÃO
                    composable(
                        route = Screen.ManterAvaliacao.route,   // <- antes era .fullRoute
                        arguments = listOf(
                            navArgument(Screen.ManterAvaliacao.ARG_ID) {
                                type = NavType.StringType; nullable = true; defaultValue = ""
                            },
                            navArgument(Screen.ManterAvaliacao.ARG_DISC) {
                                type = NavType.StringType; nullable = true; defaultValue = ""
                            }
                        )
                    ) { backStackEntry ->
                        val avaliacaoId =
                            backStackEntry.arguments?.getString(Screen.ManterAvaliacao.ARG_ID)
                                ?.ifBlank { null }
                        val disciplinaId =
                            backStackEntry.arguments?.getString(Screen.ManterAvaliacao.ARG_DISC)
                                ?.ifBlank { null }

                        ManterAvaliacaoScreen(
                            avaliacaoId = avaliacaoId,
                            disciplinaId = disciplinaId,
                            onVoltar = { navController.popBackStack() },
                            onExcluirSucessoNavegarParaLista = { navController.popBackStack() }
                        )
                    }


                    // TELA INICIAL
                    composable(Screen.TelaInicial.route) {
                        TelaInicial(navController = navController)
                    }

                    // ANOTAÇÕES
                    composable(
                        route = Screen.Anotacoes.route,
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val disciplinaId = backStackEntry.arguments?.getLong("id")
                        if (disciplinaId != null) {
                            com.example.unihub.ui.Anotacoes.AnotacoesView(
                                disciplinaId = disciplinaId,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    // ESQUECI SENHA
                    composable(Screen.EsqueciSenha.route) {
                        TelaEsqueciSenha(navController = navController)
                    }

                    // REDEFINIR SENHA (DEEP LINK)
                    composable(
                        route = Screen.RedefinirSenha.route,
                        arguments = listOf(
                            navArgument("token") { type = NavType.StringType }
                        ),
                        deepLinks = listOf(
                            navDeepLink { uriPattern = "unihub://reset?token={token}" }
                        )
                    ) { backStackEntry ->
                        val token = backStackEntry.arguments?.getString("token").orEmpty()
                        TelaRedefinirSenha(
                            token = token,
                            navController = navController
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (::navController.isInitialized) {
            navController.handleDeepLink(intent)
        }
    }
}



