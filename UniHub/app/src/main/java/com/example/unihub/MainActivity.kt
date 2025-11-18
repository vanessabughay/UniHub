package com.example.unihub

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.notificacoes.ui.NotificacoesScreen
import com.example.unihub.components.MenuLayout
import com.example.unihub.data.api.NotificacoesApi
import com.example.unihub.data.api.TarefaApi
import com.example.unihub.data.apiBackend.ApiAusenciaBackend
import com.example.unihub.data.apiBackend.ApiAvaliacaoBackend
import com.example.unihub.data.apiBackend.ApiCategoriaBackend
import com.example.unihub.data.apiBackend.ApiColunaBackend
import com.example.unihub.data.apiBackend.ApiContatoBackend
import com.example.unihub.data.apiBackend.ApiDisciplinaBackend
import com.example.unihub.data.apiBackend.ApiGrupoBackend
import com.example.unihub.data.apiBackend.ApiQuadroBackend
import com.example.unihub.data.apiBackend.ApiTarefaBackend
import com.example.unihub.data.config.RetrofitClient
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.repository.AusenciaRepository
import com.example.unihub.data.repository.AvaliacaoRepository
import com.example.unihub.data.repository.CategoriaRepository
import com.example.unihub.data.repository.ColunaRepository
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.data.repository.GoogleCalendarRepository
import com.example.unihub.data.repository.GrupoRepository
import com.example.unihub.data.repository.NotificacoesRepository
import com.example.unihub.data.repository.NotificationHistoryRepository
import com.example.unihub.data.repository.QuadroRepository
import com.example.unihub.data.repository.TarefaRepository
import com.example.unihub.notifications.FrequenciaNotificationScheduler
import com.example.unihub.notifications.CompartilhamentoNotificationSynchronizer
import com.example.unihub.notifications.ContatoNotificationSynchronizer
import com.example.unihub.notifications.AvaliacaoNotificationScheduler
import com.example.unihub.notifications.TarefaNotificationScheduler
import com.example.unihub.ui.Anotacoes.AnotacoesView
import com.example.unihub.ui.Calendario.CalendarioRoute
import com.example.unihub.ui.Calendario.CalendarioViewModel
import com.example.unihub.ui.Calendario.CalendarioViewModelFactory
import com.example.unihub.ui.HistoricoNotificacoes.HistoricoNotificacoesScreen
import com.example.unihub.ui.ListarAvaliacao.ListarAvaliacaoScreen
import com.example.unihub.ui.ListarContato.ListarContatoScreen
import com.example.unihub.ui.ListarDisciplinas.ListarDisciplinasScreen
import com.example.unihub.ui.ListarGrupo.ListarGrupoScreen
import com.example.unihub.ui.ListarQuadros.ListarQuadrosScreen
import com.example.unihub.ui.ListarQuadros.ListarQuadrosViewModelFactory
import com.example.unihub.ui.Login.LoginScreen
import com.example.unihub.ui.ManterAusencia.ManterAusenciaScreen
import com.example.unihub.ui.ManterAusencia.ManterAusenciaViewModel
import com.example.unihub.ui.ManterAusencia.ManterAusenciaViewModelFactory
import com.example.unihub.ui.ManterColuna.ColunaFormScreen
import com.example.unihub.ui.ManterColuna.ManterColunaFormViewModelFactory
import com.example.unihub.ui.ManterContato.ManterContatoScreen
import com.example.unihub.ui.ManterDisciplina.ManterDisciplinaScreen
import com.example.unihub.ui.ManterDisciplina.ManterDisciplinaViewModel
import com.example.unihub.ui.ManterDisciplina.ManterDisciplinaViewModelFactory
import com.example.unihub.ui.ManterGrupo.ManterGrupoScreen
import com.example.unihub.ui.ManterInstituicao.ManterInstituicaoScreen
import com.example.unihub.ui.ManterQuadro.QuadroFormScreen
import com.example.unihub.ui.ManterQuadro.QuadroFormViewModelFactory
import com.example.unihub.ui.ManterTarefa.ManterTarefaViewModelFactory
import com.example.unihub.ui.ManterTarefa.TarefaFormScreen
import com.example.unihub.ui.ManterConta.ManterContaScreen
import com.example.unihub.ui.ManterAvaliacao.ManterAvaliacaoScreen
import com.example.unihub.ui.Notificacoes.NotificacoesViewModelFactory
import com.example.unihub.ui.PesoNotas.ManterPesoNotasScreen
import com.example.unihub.ui.Registro.RegisterScreen
import com.example.unihub.ui.TelaEsqueciSenha.TelaEsqueciSenha
import com.example.unihub.ui.TelaEsqueciSenha.TelaRedefinirSenha
import com.example.unihub.ui.TelaInicial.TelaInicial
import com.example.unihub.ui.VisualizarDisciplina.VisualizarDisciplinaScreen
import com.example.unihub.ui.VisualizarDisciplina.VisualizarDisciplinaViewModel
import com.example.unihub.ui.VisualizarDisciplina.VisualizarDisciplinaViewModelFactory
import com.example.unihub.ui.VisualizarQuadro.VisualizarQuadroScreen
import com.example.unihub.ui.VisualizarQuadro.VisualizarQuadroViewModelFactory
import com.example.unihub.data.repository.InstituicaoRepositoryProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.flow.MutableStateFlow

// Definição das telas e suas rotas
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object TelaInicial : Screen("tela_inicial")
    object HistoricoNotificacoes : Screen("historico_notificacoes")
    object GerenciarNotificacoes : Screen("gerenciar_notificacoes")

    object ListarDisciplinas : Screen("lista_disciplinas")
    object ListarQuadros : Screen("lista_quadros")

    object ManterColuna : Screen("colunaForm/{quadroId}/{colunaId}") {
        fun createRoute(quadroId: String, colunaId: String = "new") =
            "colunaForm/$quadroId/$colunaId"
    }

    object ManterTarefa : Screen("tarefaForm/{quadroId}/{colunaId}/{tarefaId}") {
        fun createRoute(quadroId: String, colunaId: String, tarefaId: String = "new") =
            "tarefaForm/$quadroId/$colunaId/$tarefaId"
    }

    object ManterQuadro : Screen("manter_quadro?id={id}") {
        fun createRoute(id: String?) = if (id != null) "manter_quadro?id=$id" else "manter_quadro"
    }

    object ManterDisciplina : Screen("manter_disciplina?id={id}") {
        fun createRoute(id: String?) = if (id != null) "manter_disciplina?id=$id" else "manter_disciplina"
    }

    object VisualizarDisciplina : Screen("visualizar_disciplina/{id}") {
        fun createRoute(id: String) = "visualizar_disciplina/$id"
    }

    object VisualizarQuadro : Screen("visualizar_quadro/{id}") {
        fun createRoute(id: String) = "visualizar_quadro/$id"
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
        Screen("manter_instituicao?nome={nome}&media={media}&frequencia={frequencia}&mensagem={mensagem}&forcar={forcar}") {
        fun createRoute(
            nome: String,
            media: String,
            frequencia: String,
            mensagem: String = "",
            forcarPreenchimento: Boolean = false
        ) =
            "manter_instituicao?nome=${Uri.encode(nome)}&media=${Uri.encode(media)}&frequencia=${
                Uri.encode(frequencia)
            }&mensagem=${Uri.encode(mensagem)}&forcar=$forcarPreenchimento"
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

    object PesoNotas : Screen("peso_notas?disciplinaId={disciplinaId}") {
        fun createRoute(disciplinaId: String) = "peso_notas?disciplinaId=$disciplinaId"
    }

    object Calendario : Screen("calendario")
}

class MainActivity : ComponentActivity() {

    private lateinit var navController: NavHostController
    private val navigationIntentFlow = MutableStateFlow<Intent?>(null)

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TokenManager.loadToken(applicationContext)
        if (TokenManager.usuarioId != null) {
            CompartilhamentoNotificationSynchronizer.triggerImmediate(applicationContext)
            ContatoNotificationSynchronizer.triggerImmediate(applicationContext)
        }
        navigationIntentFlow.value = intent
        requestNotificationPermissionIfNeeded()

        setContent {
            MaterialTheme {
                navController = rememberNavController()
                val startDest = if (TokenManager.token.isNullOrBlank())
                    Screen.Login.route
                else
                    Screen.TelaInicial.route

                val navIntent by navigationIntentFlow.collectAsState()
                val navBackStackEntry by navController.currentBackStackEntryAsState()

                val excludedBackgroundRoutes = remember {
                    setOf(
                        Screen.Login.route,
                        Screen.Register.route,
                        Screen.EsqueciSenha.route
                    )
                }
                val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("?")
                val activeRoute = currentRoute ?: startDest.substringBefore("?")

                val baseColorScheme = MaterialTheme.colorScheme
                val typography = MaterialTheme.typography
                val shapes = MaterialTheme.shapes
                val authenticatedSurfaceColor = Color(0xFFF1F0F0)
                val colorSchemeForRoute = remember(activeRoute, baseColorScheme) {
                    if (activeRoute in excludedBackgroundRoutes) {
                        baseColorScheme
                    } else {
                        baseColorScheme.copy(
                            background = authenticatedSurfaceColor,
                            surface = authenticatedSurfaceColor,
                            surfaceDim = authenticatedSurfaceColor,
                            surfaceBright = authenticatedSurfaceColor,
                            surfaceContainerLowest = authenticatedSurfaceColor,
                            surfaceContainerLow = authenticatedSurfaceColor,
                            surfaceContainer = authenticatedSurfaceColor,
                            surfaceContainerHigh = authenticatedSurfaceColor,
                            surfaceContainerHighest = authenticatedSurfaceColor
                        )
                    }
                }

                MaterialTheme(
                    colorScheme = colorSchemeForRoute,
                    typography = typography,
                    shapes = shapes
                ) {
                    // Mantém deep links e intents de notificação
                    LaunchedEffect(navIntent) {
                        navIntent?.let { pendingIntent ->
                            navController.handleDeepLink(pendingIntent)
                            handleNotificationIntent(pendingIntent, navController)
                            navigationIntentFlow.value = null
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val routesWithoutMenu = remember {
                            setOf(
                                Screen.Login.route,
                                Screen.Register.route,
                                Screen.TelaInicial.route,
                                Screen.EsqueciSenha.route,
                                Screen.RedefinirSenha.route.substringBefore("?"),
                                Screen.ManterInstituicao.route.substringBefore("?")
                            )
                        }
                        val currentMenuRoute =
                            navBackStackEntry?.destination?.route?.substringBefore("?")
                        val menuEnabled =
                            currentMenuRoute != null && currentMenuRoute !in routesWithoutMenu

                        MenuLayout(
                            navController = navController,
                            enabled = menuEnabled
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
                                        onDisciplinaClick = { disciplinaId ->
                                            navController.navigate(
                                                Screen.VisualizarDisciplina.createRoute(disciplinaId)
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
                                    val context = LocalContext.current
                                    val repository = DisciplinaRepository(ApiDisciplinaBackend())
                                    val factory =
                                        ManterDisciplinaViewModelFactory(
                                            repository,
                                            InstituicaoRepositoryProvider.getRepository(context)
                                        )
                                    val viewModel: ManterDisciplinaViewModel = viewModel(factory = factory)

                                    ManterDisciplinaScreen(
                                        disciplinaId = disciplinaId,
                                        onVoltar = { navController.popBackStack() },
                                        onExcluirSucesso = {
                                            navController.popBackStack(Screen.ListarDisciplinas.route, inclusive = false)
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

                                    val disciplinaRepository = DisciplinaRepository(ApiDisciplinaBackend())
                                    val ausenciaRepository = AusenciaRepository(ApiAusenciaBackend())
                                    val factory = VisualizarDisciplinaViewModelFactory(
                                        disciplinaRepository, ausenciaRepository
                                    )
                                    val viewModel: VisualizarDisciplinaViewModel = viewModel(factory = factory)

                                    VisualizarDisciplinaScreen(
                                        disciplinaId = disciplinaId,
                                        onVoltar = { navController.popBackStack() },
                                        onNavigateToEdit = { idParaEditar ->
                                            navController.navigate(Screen.ManterDisciplina.createRoute(idParaEditar))
                                        },
                                        onNavigateToAusencias = { discId, ausId ->
                                            navController.navigate(Screen.ManterAusencia.createRoute(discId, ausId))
                                        },
                                        onNavigateToAnotacoes = { idDaDisciplina ->
                                            navController.navigate(Screen.Anotacoes.createRoute(idDaDisciplina.toLong()))
                                        },
                                        onNavigateToAddAvaliacaoParaDisciplina = { discId ->
                                            navController.navigate(
                                                Screen.ManterAvaliacao.createRoute(id = null, disciplinaId = discId)
                                            )
                                        },
                                        onNavigateToManterAvaliacao = { avaliacaoId ->
                                            navController.navigate(
                                                Screen.ManterAvaliacao.createRoute(id = avaliacaoId, disciplinaId = null)
                                            )
                                        },
                                        onNavigateToPesoNotas = { discId ->
                                            navController.navigate(Screen.PesoNotas.createRoute(discId))
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
                                    val disciplinaIdArg = backStackEntry.arguments?.getString("disciplinaId") ?: ""
                                    val ausenciaIdArg = backStackEntry.arguments?.getString("id")

                                    val ausenciaRepository = AusenciaRepository(ApiAusenciaBackend())
                                    val disciplinaRepository = DisciplinaRepository(ApiDisciplinaBackend())
                                    val categoriaRepository = CategoriaRepository(ApiCategoriaBackend())
                                    val factory = ManterAusenciaViewModelFactory(
                                        ausenciaRepository, disciplinaRepository, categoriaRepository
                                    )
                                    val viewModel: ManterAusenciaViewModel = viewModel(factory = factory)

                                    ManterAusenciaScreen(
                                        disciplinaId = disciplinaIdArg,
                                        ausenciaId = ausenciaIdArg,
                                        onVoltar = { navController.popBackStack() },
                                        viewModel = viewModel
                                    )
                                }

                                // MANTER CONTA
                                composable(Screen.ManterConta.route) {
                                    val context = LocalContext.current
                                    ManterContaScreen(
                                        onVoltar = { navController.popBackStack() },
                                        onNavigateToManterInstituicao = { nome, media, frequencia ->
                                            navController.navigate(
                                                Screen.ManterInstituicao.createRoute(nome, media, frequencia)
                                            )
                                        },
                                        onContaExcluida = {
                                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                                            val client = GoogleSignIn.getClient(context, gso)
                                            client.signOut().addOnCompleteListener {
                                                navController.navigate(Screen.Login.route) {
                                                    popUpTo(0) { inclusive = true }
                                                    launchSingleTop = true
                                                }
                                            }
                                        }
                                    )
                                }

                                // MANTER INSTITUIÇÃO
                                composable(
                                    route = Screen.ManterInstituicao.route,
                                    arguments = listOf(
                                        navArgument("nome") { type = NavType.StringType; defaultValue = "" },
                                        navArgument("media") { type = NavType.StringType; defaultValue = "" },
                                        navArgument("frequencia") { type = NavType.StringType; defaultValue = "" },
                                        navArgument("mensagem") { type = NavType.StringType; defaultValue = "" },
                                        navArgument("forcar") { type = NavType.BoolType; defaultValue = false }
                                    )
                                ) { backStackEntry ->
                                    val nomeArg = backStackEntry.arguments?.getString("nome") ?: ""
                                    val mediaArg = backStackEntry.arguments?.getString("media") ?: ""
                                    val frequenciaArg = backStackEntry.arguments?.getString("frequencia") ?: ""
                                    val mensagemArg = backStackEntry.arguments?.getString("mensagem") ?: ""
                                    val forcarArg = backStackEntry.arguments?.getBoolean("forcar") ?: false
                                    val context = LocalContext.current

                                    ManterInstituicaoScreen(
                                        onVoltar = { navController.popBackStack() },
                                        nome = nomeArg,
                                        media = mediaArg,
                                        frequencia = frequenciaArg,
                                        mensagemObrigatoria = mensagemArg,
                                        bloquearSaida = forcarArg
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
                                        onVoltar = {
                                            val voltouParaMenu = navController.popBackStack(Screen.TelaInicial.route, false)
                                            if (!voltouParaMenu) {
                                                navController.navigate(Screen.TelaInicial.route) {
                                                    popUpTo(Screen.TelaInicial.route) { inclusive = false }
                                                    launchSingleTop = true
                                                }
                                            }
                                        },
                                        onNavigateToGrupos = {
                                            navController.navigate(Screen.ListarGrupo.route)
                                        }
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
                                        onAddGrupo = { navController.navigate(Screen.ManterGrupo.createRoute(null)) },
                                        onNavigateToManterGrupo = { grupoId ->
                                            navController.navigate(Screen.ManterGrupo.createRoute(grupoId))
                                        },
                                        onVoltar = {
                                            val voltouParaMenu = navController.popBackStack(Screen.TelaInicial.route, false)
                                            if (!voltouParaMenu) {
                                                navController.navigate(Screen.TelaInicial.route) {
                                                    popUpTo(Screen.TelaInicial.route) { inclusive = false }
                                                    launchSingleTop = true
                                                }
                                            }
                                        },
                                        onNavigateToContatos = {
                                            navController.navigate(Screen.ListarContato.route)
                                        }
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

                                // LISTAR QUADROS
                                composable(Screen.ListarQuadros.route) {
                                    val quadroRepository = QuadroRepository(ApiQuadroBackend())
                                    val grupoRepository = GrupoRepository(ApiGrupoBackend())
                                    val viewModelFactory = ListarQuadrosViewModelFactory(
                                        quadroRepository, grupoRepository
                                    )

                                    ListarQuadrosScreen(
                                        navController = navController,
                                        viewModelFactory = viewModelFactory
                                    )
                                }

                                // VISUALIZAR QUADRO
                                composable(
                                    route = Screen.VisualizarQuadro.route,
                                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val quadroId = backStackEntry.arguments?.getString("id")
                                    val tarefaRepository = TarefaRepository(ApiTarefaBackend.apiService)
                                    val quadroRepository = QuadroRepository(ApiQuadroBackend())

                                    VisualizarQuadroScreen(
                                        navController = navController,
                                        quadroId = quadroId,
                                        onVoltar = { navController.popBackStack() },
                                        onNavigateToEditQuadro = { id ->
                                            navController.navigate(Screen.ManterQuadro.createRoute(id))
                                        },
                                        onNavigateToNovaColuna = { id ->
                                            navController.navigate(Screen.ManterColuna.createRoute(id))
                                        },
                                        onNavigateToEditarColuna = { qId, colunaId ->
                                            navController.navigate(Screen.ManterColuna.createRoute(qId, colunaId))
                                        },
                                        onNavigateToNovaTarefa = { qId, colunaId ->
                                            navController.navigate(Screen.ManterTarefa.createRoute(qId, colunaId))
                                        },
                                        onNavigateToEditarTarefa = { qId, cId, tId ->
                                            navController.navigate(Screen.ManterTarefa.createRoute(qId, cId, tId))
                                        },
                                        viewModelFactory = VisualizarQuadroViewModelFactory(
                                            quadroRepository, tarefaRepository
                                        )
                                    )
                                }

                                // MANTER QUADRO
                                composable(
                                    route = Screen.ManterQuadro.route,
                                    arguments = listOf(
                                        navArgument("id") {
                                            type = NavType.StringType
                                            nullable = true
                                            defaultValue = null
                                        }
                                    )
                                ) { backStackEntry ->
                                    val quadroIdArg = backStackEntry.arguments?.getString("id")
                                    val quadroId = quadroIdArg?.takeUnless { it == "new" }

                                    val quadroRepository = QuadroRepository(ApiQuadroBackend())
                                    val disciplinaRepository = DisciplinaRepository(ApiDisciplinaBackend())
                                    val contatoRepository = ContatoRepository(ApiContatoBackend())
                                    val grupoRepository = GrupoRepository(ApiGrupoBackend())

                                    val viewModelFactory = QuadroFormViewModelFactory(
                                        quadroRepository, disciplinaRepository, contatoRepository, grupoRepository
                                    )

                                    QuadroFormScreen(
                                        navController = navController,
                                        quadroId = quadroId,
                                        viewModelFactory = viewModelFactory
                                    )
                                }

                                // MANTER COLUNA
                                composable(
                                    route = Screen.ManterColuna.route,
                                    arguments = listOf(
                                        navArgument("quadroId") { type = NavType.StringType },
                                        navArgument("colunaId") { type = NavType.StringType; defaultValue = "new" }
                                    )
                                ) { backStackEntry ->
                                    val quadroId = backStackEntry.arguments?.getString("quadroId")
                                    if (quadroId.isNullOrBlank()) {
                                        navController.popBackStack(); return@composable
                                    }

                                    val colunaIdArg = backStackEntry.arguments?.getString("colunaId")
                                    val colunaId = colunaIdArg?.takeUnless { it == "new" }

                                    val colunaRepository = ColunaRepository(ApiColunaBackend.apiService)
                                    val viewModelFactory = ManterColunaFormViewModelFactory(colunaRepository)

                                    ColunaFormScreen(
                                        navController = navController,
                                        quadroId = quadroId,
                                        colunaId = colunaId,
                                        viewModelFactory = viewModelFactory
                                    )
                                }

                                // MANTER TAREFA
                                composable(
                                    route = Screen.ManterTarefa.route,
                                    arguments = listOf(
                                        navArgument("quadroId") { type = NavType.StringType },
                                        navArgument("colunaId") { type = NavType.StringType },
                                        navArgument("tarefaId") { type = NavType.StringType }
                                    )
                                ) { backStackEntry ->
                                    val quadroId = backStackEntry.arguments?.getString("quadroId")
                                    val colunaId = backStackEntry.arguments?.getString("colunaId")
                                    if (quadroId.isNullOrBlank() || colunaId.isNullOrBlank()) {
                                        navController.popBackStack(); return@composable
                                    }

                                    val tarefaIdArg = backStackEntry.arguments?.getString("tarefaId")
                                    val tarefaId = tarefaIdArg?.takeUnless { it == "new" || it.isBlank() }

                                    val tarefaRepository = TarefaRepository(ApiTarefaBackend.apiService)
                                    val quadroRepository = QuadroRepository(ApiQuadroBackend())
                                    val grupoRepository = GrupoRepository(ApiGrupoBackend())
                                    val contatoRepository = ContatoRepository(ApiContatoBackend())
                                    val viewModelFactory = ManterTarefaViewModelFactory(
                                        tarefaRepository, quadroRepository, grupoRepository, contatoRepository
                                    )

                                    TarefaFormScreen(
                                        navController = navController,
                                        quadroId = quadroId,
                                        colunaId = colunaId,
                                        tarefaId = tarefaId,
                                        viewModelFactory = viewModelFactory
                                    )
                                }

                                // LISTAR AVALIAÇÃO
                                composable(Screen.ListarAvaliacao.route) {
                                    ListarAvaliacaoScreen(
                                        onAddAvaliacaoGeral = {
                                            navController.navigate(
                                                Screen.ManterAvaliacao.createRoute(id = null, disciplinaId = null)
                                            )
                                        },
                                        onAddAvaliacaoParaDisciplina = { disciplinaId ->
                                            navController.navigate(
                                                Screen.ManterAvaliacao.createRoute(id = null, disciplinaId = disciplinaId)
                                            )
                                        },
                                        onNavigateToManterAvaliacao = { avaliacaoId ->
                                            navController.navigate(
                                                Screen.ManterAvaliacao.createRoute(id = avaliacaoId, disciplinaId = null)
                                            )
                                        },
                                        onVoltar = { navController.popBackStack() }
                                    )
                                }

                                // PESO NOTAS
                                composable(
                                    route = Screen.PesoNotas.route,
                                    arguments = listOf(navArgument("disciplinaId") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val discId = backStackEntry.arguments?.getString("disciplinaId")!!
                                    ManterPesoNotasScreen(
                                        disciplinaId = discId,
                                        onVoltar = { navController.popBackStack() },
                                        onAddAvaliacaoParaDisciplina = { id ->
                                            navController.navigate(
                                                Screen.ManterAvaliacao.createRoute(id = null, disciplinaId = id)
                                            )
                                        },
                                        onEditarAvaliacao = { avaliacaoId, disciplinaId ->
                                            navController.navigate(
                                                Screen.ManterAvaliacao.createRoute(id = avaliacaoId, disciplinaId = disciplinaId)
                                            )
                                        }
                                    )
                                }

                                // MANTER AVALIAÇÃO
                                composable(
                                    route = Screen.ManterAvaliacao.route,
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
                                        backStackEntry.arguments?.getString(Screen.ManterAvaliacao.ARG_ID)?.ifBlank { null }
                                    val disciplinaId =
                                        backStackEntry.arguments?.getString(Screen.ManterAvaliacao.ARG_DISC)?.ifBlank { null }

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

                                // HISTÓRICO DE NOTIFICAÇÕES
                                composable(Screen.HistoricoNotificacoes.route) {
                                    HistoricoNotificacoesScreen(onVoltar = { navController.popBackStack() })
                                }

                                // GERENCIAR NOTIFICAÇÕES (versão completa com Retrofit + TarefaScheduler)
                                composable(Screen.GerenciarNotificacoes.route) {
                                    val context = LocalContext.current

                                    val notificacoesApi = RetrofitClient.create(NotificacoesApi::class.java)
                                    val tarefaApi = RetrofitClient.create(TarefaApi::class.java)

                                    val tarefaRepository = TarefaRepository(tarefaApi)

                                    val frequenciaScheduler = FrequenciaNotificationScheduler(context)
                                    val avaliacaoScheduler = AvaliacaoNotificationScheduler(context)
                                    val tarefaScheduler = TarefaNotificationScheduler(context)

                                    val disciplinaRepository = DisciplinaRepository(ApiDisciplinaBackend())
                                    val avaliacaoRepository = AvaliacaoRepository(ApiAvaliacaoBackend())

                                    val notificacoesRepository = NotificacoesRepository(
                                        api = notificacoesApi,
                                        frequenciaScheduler = frequenciaScheduler,
                                        avaliacaoScheduler = avaliacaoScheduler,
                                        tarefaScheduler = tarefaScheduler,
                                        disciplinaRepository = disciplinaRepository,
                                        avaliacaoRepository = avaliacaoRepository,
                                        tarefaRepository = tarefaRepository,
                                        notificationHistoryRepository = NotificationHistoryRepository.getInstance(context),
                                        compartilhamentoSynchronizer = CompartilhamentoNotificationSynchronizer.getInstance(context),
                                        contatoSynchronizer = ContatoNotificationSynchronizer.getInstance(context)
                                    )

                                    val factory = NotificacoesViewModelFactory(notificacoesRepository)

                                    NotificacoesScreen(
                                        viewModel = viewModel(factory = factory),
                                        onBack = { navController.popBackStack() },
                                        onAbrirHistorico = { navController.navigate(Screen.HistoricoNotificacoes.route) }
                                    )
                                }

                                // ANOTAÇÕES
                                composable(
                                    route = Screen.Anotacoes.route,
                                    arguments = listOf(navArgument("id") { type = NavType.LongType })
                                ) { backStackEntry ->
                                    val disciplinaId = backStackEntry.arguments?.getLong("id")
                                    if (disciplinaId != null) {
                                        AnotacoesView(
                                            disciplinaId = disciplinaId,
                                            onVoltar = { navController.popBackStack() }
                                        )
                                    }
                                }

                                // CALENDARIO
                                composable(Screen.Calendario.route) {
                                    val context = LocalContext.current
                                    val avaliacaoRepository = AvaliacaoRepository(ApiAvaliacaoBackend())
                                    val googleCalendarRepository = GoogleCalendarRepository()
                                    val factory = CalendarioViewModelFactory(
                                        avaliacaoRepository, googleCalendarRepository, context
                                    )
                                    val viewModel: CalendarioViewModel = viewModel(factory = factory)

                                    CalendarioRoute(
                                        viewModel = viewModel,
                                        onNovaAvaliacao = {
                                            navController.navigate(
                                                Screen.ManterAvaliacao.createRoute(id = null, disciplinaId = null)
                                            )
                                        },
                                        onAvaliacaoClick = { avaliacaoId ->
                                            navController.navigate(
                                                Screen.ManterAvaliacao.createRoute(
                                                    id = avaliacaoId.toString(),
                                                    disciplinaId = null
                                                )
                                            )
                                        },
                                        onVoltar = { navController.popBackStack() }
                                    )
                                }

                                // ESQUECI SENHA
                                composable(Screen.EsqueciSenha.route) {
                                    TelaEsqueciSenha(navController = navController)
                                }

                                // REDEFINIR SENHA (DEEP LINK)
                                composable(
                                    route = Screen.RedefinirSenha.route,
                                    arguments = listOf(navArgument("token") { type = NavType.StringType }),
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
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        navigationIntentFlow.value = intent
    }

    private fun handleNotificationIntent(intent: Intent, navController: NavHostController) {
        if (TokenManager.token.isNullOrBlank()) return

        val targetScreen = intent.getStringExtra(EXTRA_TARGET_SCREEN)

        if (targetScreen == TARGET_SCREEN_LISTAR_QUADROS) {
            navController.navigate(Screen.ListarQuadros.route) { launchSingleTop = true }
            intent.removeExtra(EXTRA_TARGET_SCREEN)
            return
        }

        val avaliacaoId = intent.getStringExtra(EXTRA_TARGET_AVALIACAO_ID)
        if (!avaliacaoId.isNullOrBlank()) {
            val disciplinaId = intent.getStringExtra(EXTRA_TARGET_DISCIPLINA_ID)
            val route = Screen.ManterAvaliacao.createRoute(id = avaliacaoId, disciplinaId = disciplinaId)

            navController.navigate(route) { launchSingleTop = true }

            intent.removeExtra(EXTRA_TARGET_AVALIACAO_ID)
            intent.removeExtra(EXTRA_TARGET_DISCIPLINA_ID)
            intent.removeExtra(EXTRA_TARGET_SCREEN)
            return
        }

        val disciplinaId = intent.getStringExtra(EXTRA_TARGET_DISCIPLINA_ID) ?: return

        val route = if (targetScreen == TARGET_SCREEN_REGISTRAR_AUSENCIA) {
            Screen.ManterAusencia.createRoute(disciplinaId, null)
        } else {
            Screen.VisualizarDisciplina.createRoute(disciplinaId)
        }

        navController.navigate(route) { launchSingleTop = true }

        intent.removeExtra(EXTRA_TARGET_DISCIPLINA_ID)
        intent.removeExtra(EXTRA_TARGET_SCREEN)
    }
    private fun requestNotificationPermissionIfNeeded() {
        // Só precisa pedir em Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_POST_NOTIFICATIONS
                )
            }
        }
    }


    companion object {
        const val EXTRA_TARGET_DISCIPLINA_ID = "extra_target_disciplina_id"
        const val EXTRA_TARGET_AVALIACAO_ID = "extra_target_avaliacao_id"
        const val EXTRA_TARGET_SCREEN = "extra_target_screen"
        const val TARGET_SCREEN_VISUALIZAR_DISCIPLINA = "visualizar_disciplina"
        const val TARGET_SCREEN_REGISTRAR_AUSENCIA = "registrar_ausencia"
        const val TARGET_SCREEN_VISUALIZAR_AVALIACAO = "visualizar_avaliacao"
        const val TARGET_SCREEN_LISTAR_QUADROS = "listar_quadros"
        const val EXTRA_TARGET_TAREFA_ID = "extra_target_tarefa_id"
        const val TARGET_SCREEN_VISUALIZAR_TAREFA = "visualizar_tarefa"
        private const val REQUEST_CODE_POST_NOTIFICATIONS = 1001
    }
}
