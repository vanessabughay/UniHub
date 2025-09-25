package com.example.unihub.ui.register // Pacote adaptado para o registro

import com.example.unihub.components.CustomLabeledInput
import android.widget.Toast
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
import com.example.unihub.data.repository.InstituicaoRepositoryProvider
import androidx.navigation.NavController
import com.example.unihub.ui.register.RegisterViewModel // Importação do ViewModel específico de registro

@Composable
fun RegisterScreen(navController: NavController, viewModel: RegisterViewModel = viewModel()) {

    val context = LocalContext.current

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(viewModel.success) {
        if (viewModel.success) {
            val repository = InstituicaoRepositoryProvider.getRepository(context)
            Toast.makeText(context, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
            viewModel.success = false
            navController.navigate("login") {
                popUpTo("register") { inclusive = true }
            }
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
                        text = "Cadastre-se",
                        fontSize = 32.sp,
                        color = Color(0xFF234A6A), // Cor de texto do UniHub
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    CustomLabeledInput("Nome", viewModel.name) { viewModel.name = it }
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomLabeledInput("E-mail", viewModel.email) { viewModel.email = it }
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomLabeledInput("Senha", viewModel.password, isPassword = true) { viewModel.password = it }
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomLabeledInput("Confirme a Senha", viewModel.confirmPassword, isPassword = true) { viewModel.confirmPassword = it }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            viewModel.registerUser()
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
                                text = "Cadastrar",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Já possui conta?",
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Entrar",
                    color = Color(0xFF234A6A), // Cor de link do UniHub
                    fontSize = 13.sp,
                    modifier = Modifier.clickable {
                        navController.navigate("login")
                    }
                )
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun Preview_RegisterScreen() {
    MaterialTheme {
        RegisterScreen(
            navController = NavController(LocalContext.current),
            viewModel = object : RegisterViewModel() {
                override var name by remember { mutableStateOf("Nome de Exemplo") }
                override var email by remember { mutableStateOf("exemplo@email.com") }
                override var password by remember { mutableStateOf("senha123") }
                override var confirmPassword by remember { mutableStateOf("senha123") }
                override var isLoading by remember { mutableStateOf(false) }
                override var errorMessage by remember { mutableStateOf<String?>(null) }
                override var success by remember { mutableStateOf(false) }

                override fun registerUser() {}
            }
        )
    }
}