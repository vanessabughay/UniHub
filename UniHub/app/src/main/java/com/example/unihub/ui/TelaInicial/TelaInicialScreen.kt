package com.example.unihub.ui.TelaInicial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.*
import androidx.navigation.NavHostController


/* ====== Paleta de cores (View) ====== */
private object CoresApp {
    val Fundo = Color(0xFFF6F7F8)
    val AzulNevoa = Color(0xFFE7F1F6)
    val AzulIcone = Color(0xFF234A6A)
    val AzulMenu = Color(0xFF4D6C8B)
    val AzulCartao = Color(0xE6E2EFF4)
    val TextoPrincipal = Color(0xFF1F2937)
    val TextoSecundario = Color(0xFF6B7280)
    val Divisor = Color(0xFFE5E7EB)
}

/* ====== Entrada da tela com ViewModel ====== */
@Composable
fun TelaInicial(
    navController: NavHostController,
    viewModel: TelaInicialViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsStateWithLifecycleCompat()

    LaunchedEffect(Unit) {
        // Chama a função para filtrar avaliações e tarefas logo após o carregamento da tela
        viewModel.filtrarAvaliacoesEValidarTarefas()

        viewModel.eventoNavegacao.collect { destino ->
            when (destino.lowercase()) {
                //"projetos" -> navController.navigate("projetos")
                //"calendário" -> navController.navigate("calendario")
                "disciplinas" -> navController.navigate("lista_disciplinas")
                //"avaliações" -> navController.navigate("avaliacoes")
                "perfil" -> navController.navigate("manter_conta")
               // "serviço de nuvem" -> navController.navigate("servico_nuvem")
                "contatos" -> navController.navigate("lista_contato")
                "grupos" -> navController.navigate("lista_grupo")
                //"configurar notificações" -> navController.navigate("configurar_notificacoes")
                //"atividades" -> navController.navigate("atividades")
                else -> {} // fallback
            }
        }
    }


    TelaInicialView(
        estado = estado,
        onAbrirMenu = { viewModel.abrirMenu() },
        onFecharMenu = { viewModel.fecharMenu() },
        onAlternarMenu = { viewModel.alternarMenu() },
        onClicarAtalho = { viewModel.aoClicarAtalho(it) },
        onClicarOpcaoMenu = { viewModel.aoClicarOpcaoMenu(it) },
        onAlternarSecaoAvaliacoes = { viewModel.alternarSecaoAvaliacoes() },
        onAlternarSecaoTarefas = { viewModel.alternarSecaoTarefas() }
    )
}

/* ====== Versão pura (View) ====== */
@Composable
fun TelaInicialView(
    estado: EstadoTelaInicial,
    onAbrirMenu: () -> Unit,
    onFecharMenu: () -> Unit,
    onAlternarMenu: () -> Unit,
    onClicarAtalho: (String) -> Unit,
    onClicarOpcaoMenu: (String) -> Unit,
    onAlternarSecaoAvaliacoes: () -> Unit,
    onAlternarSecaoTarefas: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CoresApp.Fundo)
            .systemBarsPadding()
    ) {
        ConteudoPrincipal(
            estado = estado,
            onAbrirMenu = onAbrirMenu,
            onAlternarSecaoAvaliacoes = onAlternarSecaoAvaliacoes,
            onAlternarSecaoTarefas = onAlternarSecaoTarefas,
            onClicarAtalho = onClicarAtalho,
            onFecharMenu = onFecharMenu,
            onAlternarMenu = onAlternarMenu,
            onClicarOpcaoMenu = onClicarOpcaoMenu
        )


        /* Scrim */
        AnimatedVisibility(visible = estado.menuAberto, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFCCE5FF).copy(alpha = 0.4f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onFecharMenu() }
            )
        }

        /* Menu lateral à direita */
        AnimatedVisibility(
            visible = estado.menuAberto,
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
                .align(Alignment.TopEnd)   // alinhando o AnimatedVisibility
        ) {
            MenuLateral(
                opcoes = estado.opcoesMenu,
                onFechar = onFecharMenu,
                onClicarOpcao = onClicarOpcaoMenu,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}

/* ====== Blocos de UI ====== */
@Composable
private fun ConteudoPrincipal(
    estado: EstadoTelaInicial,
    onAbrirMenu: () -> Unit,
    onClicarAtalho: (String) -> Unit,
    onAlternarSecaoAvaliacoes: () -> Unit,
    onAlternarSecaoTarefas: () -> Unit,
    onFecharMenu: () -> Unit,
    onAlternarMenu: () -> Unit,
    onClicarOpcaoMenu: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        CabecalhoPerfil(
            nome = estado.usuario.nome,
            atalhos = estado.atalhosRapidos,
            onAbrirMenu = onAbrirMenu,
            onClicarAtalho = onClicarAtalho
        )
        ConteudoAbaixoDoTopo(
            estado = estado,
            onAlternarSecaoAvaliacoes = onAlternarSecaoAvaliacoes,
            onAlternarSecaoTarefas = onAlternarSecaoTarefas,
        )
    }
}

@Composable
private fun CabecalhoPerfil(
    nome: String,
    atalhos: List<String>,
    onAbrirMenu: () -> Unit,
    onClicarAtalho: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                CoresApp.AzulNevoa,
                RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            )
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp)) {

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.School, contentDescription = "Avatar", tint = CoresApp.AzulIcone, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.width(14.dp))
                Text(
                    text = nome,
                    color = CoresApp.AzulIcone,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = { /* notificações */ }) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notificações", tint = CoresApp.AzulIcone)
                }
                Box(modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.65f))) {
                    IconButton(onClick = onAbrirMenu) {
                        Icon(Icons.Outlined.Menu, contentDescription = "Abrir menu", tint = Color(0xFF274B6F))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(text = "Menus rápidos", color = CoresApp.TextoPrincipal, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Divider(modifier = Modifier.padding(top = 8.dp), thickness = 1.dp, color = CoresApp.Divisor.copy(alpha = 0.7f))
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                atalhos.forEach { rotulo ->
                    AtalhoRapido(
                        icone = iconeParaRotulo(rotulo),
                        rotulo = rotulo,
                        onClick = { onClicarAtalho(rotulo) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AtalhoRapido(icone: ImageVector, rotulo: String, onClick: () -> Unit) {
    Column(modifier = Modifier
        .width(80.dp)
        .clickable { onClick() }, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icone, contentDescription = rotulo, tint = CoresApp.AzulIcone, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(text = rotulo, color = CoresApp.AzulIcone, fontSize = 14.sp)
    }
}

@Composable
private fun ConteudoAbaixoDoTopo(
    estado: EstadoTelaInicial,
    onAlternarSecaoAvaliacoes: () -> Unit,
    onAlternarSecaoTarefas: () -> Unit
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(18.dp))
        TituloDeSecao(
            titulo = "Próximas avaliações",
            setaAbaixo = estado.secaoTarefasAberta,
            onClick = onAlternarSecaoAvaliacoes)
        Spacer(Modifier.height(10.dp))

        AnimatedVisibility(visible = estado.secaoAvaliacoesAberta) {
            Column {
                estado.avaliacoes.forEachIndexed { i, a -> // Use o estado
                    CartaoAvaliacao(a.diaSemana, a.dataCurta, a.titulo, a.descricao)
                    if (i != estado.avaliacoes.lastIndex) Spacer(Modifier.height(14.dp))
                }
            }
        }

        Spacer(Modifier.height(26.dp))
        TituloDeSecao(
            titulo = "Próximas tarefas",
            setaAbaixo = estado.secaoTarefasAberta,
            onClick = onAlternarSecaoTarefas
        )
        Spacer(Modifier.height(10.dp))

        AnimatedVisibility(visible = estado.secaoTarefasAberta) {
            Column {
                estado.tarefas.forEachIndexed { i, t ->
                    CartaoAvaliacao(t.diaSemana, t.dataCurta, t.titulo, t.descricao)
                    if (i != estado.tarefas.lastIndex) Spacer(Modifier.height(14.dp))
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun TituloDeSecao(titulo: String, setaAbaixo: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        Icon(if (setaAbaixo) Icons.Outlined.ExpandMore else Icons.Outlined.ChevronRight, contentDescription = null, tint = CoresApp.TextoPrincipal)
        Spacer(Modifier.width(6.dp))
        Text(text = titulo, color = CoresApp.TextoPrincipal, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
    }
    Divider(modifier = Modifier.padding(top = 10.dp), color = CoresApp.Divisor)
}

@Composable
private fun CartaoAvaliacao(diaSemana: String, dataCurta: String, titulo: String, descricao: String) {
    Surface(
        color = CoresApp.AzulCartao,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 110.dp)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.widthIn(min = 110.dp), horizontalAlignment = Alignment.Start) {
                Text(text = diaSemana, color = CoresApp.TextoPrincipal, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Spacer(Modifier.height(8.dp))
                Text(text = dataCurta, color = CoresApp.TextoSecundario, fontSize = 16.sp)
            }
            Divider(modifier = Modifier
                .height(56.dp)
                .width(1.dp), color = Color(0xFFCBD5E1))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = titulo, color = CoresApp.TextoPrincipal, fontWeight = FontWeight.Bold, fontSize = 22.sp, maxLines = 2)
                Spacer(Modifier.height(6.dp))
                Text(text = descricao, color = CoresApp.TextoSecundario, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun LinhaListaSimples(titulo: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = CoresApp.TextoPrincipal)
            Spacer(Modifier.width(8.dp))
            Text(text = titulo, color = CoresApp.TextoPrincipal, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }
        Divider(color = CoresApp.Divisor)
    }
}

@Composable
private fun MenuLateral(
    opcoes: List<String>,
    onFechar: () -> Unit,
    onClicarOpcao: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = CoresApp.AzulMenu,
        shape = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 22.dp)) {
            Text(text = "Menu", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(14.dp))

            opcoes.forEach { rotulo ->
                ItemMenu(rotulo, iconeParaRotulo(rotulo), onClick = { onClicarOpcao(rotulo) })
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onFechar) {
                Text(text = "Sair", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            }

            /* FUNÇÃO PARA LOGOUT ADAPTAR, SUBSTITUIR ONFECHAR!!!  performLogout()
            fun performLogout() {
                // Realiza uma chamada para o backend para efetuar o logout
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val response = api.logout()
                        if (response.isSuccessful) {
                            // Se o logout for bem-sucedido, pode redirecionar o usuário
                            navController.navigate("login_screen")
                        }
                    } catch (e: Exception) {

                    }
                }
            }
            */
        }
    }
}

@Composable
private fun ItemMenu(texto: String, icone: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icone, contentDescription = texto, tint = Color.White.copy(alpha = 0.95f))
        Spacer(Modifier.width(12.dp))
        Text(text = texto, color = Color.White.copy(alpha = 0.95f), fontSize = 20.sp)
    }
}

/* ====== Mapeamentos de ícones ====== */
private fun iconeParaRotulo(rotulo: String): ImageVector = when (rotulo.lowercase()) {
    "projetos" -> Icons.Outlined.Groups
    "calendário" -> Icons.Outlined.CalendarMonth
    "disciplinas" -> Icons.Outlined.MenuBook
    "avaliações" -> Icons.Outlined.RateReview
    "perfil" -> Icons.Outlined.Person
    "serviço de nuvem" -> Icons.Outlined.CloudQueue
    "contatos" -> Icons.Outlined.Contacts
    "grupos" -> Icons.Outlined.Groups
    "configurar" -> Icons.Outlined.Settings
    "notificações" -> Icons.Outlined.Notifications
    "atividades" -> Icons.Outlined.Assignment
    else -> Icons.Outlined.Circle
}

/* ====== Preview sem ViewModel (estado fake) ====== */
@Preview(showBackground = true, backgroundColor = 0xFFF6F7F8, widthDp = 360, heightDp = 800)
@Composable
private fun Preview_TelaInicialView() {
    val estadoExemplo = EstadoTelaInicial(
        usuario = Usuario("Paulo Cueto"),
        menuAberto = false,
        avaliacoes = listOf(
            Avaliacao("Quarta", "27/03", "Prova 1", "Estrutura de dados"),
            Avaliacao("Segunda", "01/04", "Trabalho Microsserviços", "Desenvolvimento Web II")
        ),
        opcoesMenu = listOf(
            "Perfil", "Disciplinas", "Serviço de nuvem", "Calendário", "Contatos",
            "Grupos", "Projetos", "Configurar", "notificações", "Atividades"
        ),
        atalhosRapidos = listOf("Projetos", "Calendário", "Disciplinas", "Avaliações")
    )

    MaterialTheme {
        TelaInicialView(
            estado = estadoExemplo,
            onAbrirMenu = {},
            onFecharMenu = {},
            onAlternarMenu = {},
            onClicarAtalho = {},
            onClicarOpcaoMenu = {},
            onAlternarSecaoAvaliacoes = {},
            onAlternarSecaoTarefas = { }
        )
    }
}

/* ====== Preview com menu aberto ====== */
@Preview(showBackground = true, backgroundColor = 0xFFF6F7F8, widthDp = 360, heightDp = 800)
@Composable
private fun Preview_MenuAberto() {
    val estadoExemplo = EstadoTelaInicial(
        usuario = Usuario("Paulo Cueto"),
        menuAberto = true,
        avaliacoes = listOf(
            Avaliacao("Quarta", "27/03", "Prova 1", "Estrutura de dados"),
            Avaliacao("Segunda", "01/04", "Trabalho Microsserviços", "Desenvolvimento Web II")
        ),
        opcoesMenu = listOf(
            "Perfil", "Disciplinas", "Serviço de nuvem", "Calendário", "Contatos",
            "Grupos", "Projetos", "Configurar notificações", "Atividades"
        ),
        atalhosRapidos = listOf("Projetos", "Calendário", "Disciplinas", "Avaliações")
    )

    MaterialTheme {
        TelaInicialView(
            estado = estadoExemplo,
            onAbrirMenu = {},
            onFecharMenu = {},
            onAlternarMenu = {},
            onClicarAtalho = {},
            onClicarOpcaoMenu = {},
            onAlternarSecaoAvaliacoes = { },
            onAlternarSecaoTarefas = {  }
        )
    }
}



@Composable
fun <T> StateFlow<T>.collectAsStateWithLifecycleCompat(): State<T> {
    return this.collectAsStateWithLifecycle()
}

@Composable
fun <T> StateFlow<T>.collectAsStateCompat(): State<T> {
    val initialValue = (this as? MutableStateFlow<T>)?.value
        ?: error("collectAsStateCompat requires a MutableStateFlow with an initial value")
    return this.collectAsState(initial = initialValue)
}
