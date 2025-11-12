package com.example.unihub.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.unihub.Screen
import com.example.unihub.data.config.TokenManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import java.util.Locale

/**
 * Actions exposed to child composables so they can control the lateral menu.
 */
data class MenuActions(
    val enabled: Boolean,
    val openMenu: () -> Unit,
    val closeMenu: () -> Unit,
    val toggleMenu: () -> Unit
) {
    companion object {
        val Disabled = MenuActions(
            enabled = false,
            openMenu = {},
            closeMenu = {},
            toggleMenu = {}
        )
    }
}

val LocalMenuActions = staticCompositionLocalOf { MenuActions.Disabled }

private val DefaultMenuOptions = listOf(
    "Tela inicial",
    "Perfil",
    "Disciplinas",
    "Calendário",
    "Contatos",
    "Grupos",
    "Quadros",
    "Gerenciar notificações"
)

@Composable
fun MenuLayout(
    navController: NavHostController,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val safeAreaContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            content()
        }
    }
    if (!enabled) {
        CompositionLocalProvider(LocalMenuActions provides MenuActions.Disabled) {
            safeAreaContent()
        }
        return
    }

    val menuOpenState = rememberSaveable { mutableStateOf(false) }
    val menuActions = rememberMenuActions(menuOpenState)
    val context = androidx.compose.ui.platform.LocalContext.current

    CompositionLocalProvider(LocalMenuActions provides menuActions) {
        Box(modifier = Modifier.fillMaxSize()) {
            safeAreaContent()

            AnimatedVisibility(
                visible = menuOpenState.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFCCE5FF).copy(alpha = 0.4f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            menuActions.closeMenu()
                        }
                )
            }

            AnimatedVisibility(
                visible = menuOpenState.value,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)
                    )
            ) {
                MenuLateral(
                    opcoes = DefaultMenuOptions,
                    onFechar = menuActions.closeMenu,
                    onClicarOpcao = { destino ->
                        menuActions.closeMenu()
                        navegarParaOpcao(navController, destino)
                    },
                    onLogout = {
                        realizarLogout(context, navController, menuActions.closeMenu)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private fun rememberMenuActions(state: MutableState<Boolean>): MenuActions {
    return MenuActions(
        enabled = true,
        openMenu = { state.value = true },
        closeMenu = { state.value = false },
        toggleMenu = { state.value = !state.value }
    )
}

private fun navegarParaOpcao(navController: NavHostController, rotulo: String) {
    val destino = when (rotulo.lowercase(Locale.getDefault())) {
        "tela inicial" -> Screen.TelaInicial.route
        "quadros" -> Screen.ListarQuadros.route
        "calendário", "calendario" -> "calendario"
        "disciplinas" -> Screen.ListarDisciplinas.route
        "avaliações", "avaliacoes" -> Screen.ListarAvaliacao.route
        "perfil" -> Screen.ManterConta.route
        "contatos" -> Screen.ListarContato.route
        "grupos" -> Screen.ListarGrupo.route
        "historico_notificacoes" -> Screen.HistoricoNotificacoes.route
        "gerenciar notificações", "gerenciar notificacoes" -> Screen.GerenciarNotificacoes.route
        else -> null
    }

    destino?.let { route ->
        navController.navigate(route) {
            launchSingleTop = true
        }
    }
}

private fun realizarLogout(
    context: Context,
    navController: NavHostController,
    onLogoutCompleted: () -> Unit
) {
    TokenManager.clearToken(context)
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
    val client = GoogleSignIn.getClient(context, gso)
    client.signOut().addOnCompleteListener {
        onLogoutCompleted()
        navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun MenuLateral(
    opcoes: List<String>,
    onFechar: () -> Unit,
    onClicarOpcao: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF4D6C8B),
        shape = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF4D6C8B))
                .padding(horizontal = 22.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Menu",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(14.dp))

                opcoes.forEach { rotulo ->
                    ItemMenu(rotulo, iconeParaRotulo(rotulo)) {
                        onClicarOpcao(rotulo)
                    }
                }
            }

            TextButton(onClick = onLogout) {
                Text(
                    text = "Sair",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun ItemMenu(
    texto: String,
    icone: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icone,
            contentDescription = texto,
            tint = Color.White.copy(alpha = 0.95f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = texto,
            color = Color.White.copy(alpha = 0.95f),
            fontSize = 20.sp
        )
    }
}

fun iconeParaRotulo(rotulo: String): ImageVector = when (rotulo.lowercase(Locale.getDefault())) {
    "tela inicial" -> Icons.Outlined.Home
    "quadros" -> Icons.Outlined.Groups
    "calendário", "calendario" -> Icons.Outlined.CalendarMonth
    "disciplinas" -> Icons.Outlined.MenuBook
    "avaliações", "avaliacoes" -> Icons.Outlined.RateReview
    "perfil" -> Icons.Outlined.Person
    "contatos" -> Icons.Outlined.Contacts
    "grupos" -> Icons.Outlined.Groups
    "configurar notificações", "gerenciar notificações", "configurar notificacoes", "gerenciar notificacoes" -> Icons.Outlined.Settings
    else -> Icons.Outlined.Circle
}