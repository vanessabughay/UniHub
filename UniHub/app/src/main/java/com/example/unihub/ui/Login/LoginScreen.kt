package com.example.unihub.ui.Login // Pacote adaptado para o UniHub

import android.widget.Toast
import com.example.unihub.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.unihub.components.CustomLabeledInput // Importação adaptada
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.unihub.Screen
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.runtime.remember
import com.example.unihub.data.repository.InstituicaoRepositoryProvider
import com.example.unihub.data.config.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    val instituicaoRepository = remember { InstituicaoRepositoryProvider.getRepository(context) }

    LaunchedEffect(viewModel.success) {
        if (viewModel.success) {
            Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()

            val reportedHasInstitution = viewModel.hasInstitution
            val previousHasInstitution = TokenManager.hasInstitution
            val instituicaoRemota = withContext(Dispatchers.IO) {
                runCatching { instituicaoRepository.instituicaoUsuario() }.getOrNull()
            }

            val possuiInstituicao = when {
                instituicaoRemota != null -> true
                reportedHasInstitution == true -> true
                previousHasInstitution -> true
                else -> false
            }
            TokenManager.updateHasInstitution(context, possuiInstituicao)


            if (!possuiInstituicao) {
                val mensagemObrigatoria = context.getString(R.string.instituicao_obrigatoria_message)
                navController.navigate(
                    Screen.ManterInstituicao.createRoute(
                        nome = "",
                        media = "",
                        frequencia = "",
                        mensagem = mensagemObrigatoria,
                        forcarPreenchimento = true
                    )
                ) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                navController.navigate(Screen.TelaInicial.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            viewModel.success = false
            viewModel.hasInstitution = possuiInstituicao
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE7F1F6)), // Cor de fundo do UniHub
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Box(
                modifier = Modifier
                    .width(360.dp)
                    .wrapContentHeight()
                    .background(
                        color = Color(0xE6E2EFF4), // Cor de cartão do UniHub
                        shape = RoundedCornerShape(50.dp)
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Login",
                        fontSize = 32.sp,
                        color = Color(0xFF234A6A), // Cor de texto do UniHub
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    CustomLabeledInput("E-mail", viewModel.email) { viewModel.email = it }
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomLabeledInput("Senha", viewModel.password, isPassword = true) { viewModel.password = it }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            viewModel.loginUser(context)
                        },
                        enabled = !viewModel.isLoading,
                        modifier = Modifier
                            .width(120.dp)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4D6C8B) // Cor de botão do UniHub
                        )
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Entrar",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 👉 ADICIONE O BOTÃO GOOGLE AQUI
                    GoogleLoginButton(viewModel)
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Não possui conta?",
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Registre-se.",
                    color = Color(0xFF234A6A), // Cor de link do UniHub
                    fontSize = 13.sp,
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Esqueceu a senha?",
                color = Color(0xFF234A6A),
                fontSize = 13.sp,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.EsqueciSenha.route)
                }
            )
        }


    }
}

@Composable
fun GoogleLoginButton(viewModel: AuthViewModel) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                Toast.makeText(context, "Não foi possível obter o ID Token", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }
            viewModel.loginWithGoogle(context, idToken)
        } catch (e: ApiException) {
            Toast.makeText(context, "Falha no Google Sign-In: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    fun startGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.server_client_id))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        launcher.launch(client.signInIntent)
    }

    Button(
        onClick = { startGoogleSignIn() },
        enabled = !viewModel.isLoading,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text("Continuar com Google")
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun Preview_LoginScreen() {
    val nav = androidx.navigation.compose.rememberNavController()

    val fakeVm = object : AuthViewModel() {
        override var email: String = "exemplo@email.com"
        override var password: String = "senha123"
        override var isLoading: Boolean = false
        override var errorMessage: String? = null
        override var success: Boolean = false
        override var hasInstitution: Boolean? = true
        override fun loginUser(context: Context) { /* no-op */ }
        override fun loginWithGoogle(context: Context, idToken: String) { /* no-op */ }
    }

    MaterialTheme {
        LoginScreen(navController = nav, viewModel = fakeVm)
    }
}



