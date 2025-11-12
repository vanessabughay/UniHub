package com.example.unihub.ui.TelaEsqueciSenha

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.unihub.data.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun TelaEsqueciSenha(
    navController: NavController,
    repository: AuthRepository = AuthRepository()
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE7F1F6))
            .padding(horizontal = 24.dp)
            .padding(top = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Esqueci minha senha",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            singleLine = true,
            label = { Text("E-mail cadastrado") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            enabled = !carregando && email.isNotBlank(),
            onClick = {
                carregando = true
                scope.launch {
                    repository.solicitarRedefinicaoSenha(
                        email = email,
                        onSuccess = {
                            carregando = false
                            Toast.makeText(
                                ctx,
                                "Se o e-mail existir, enviaremos o link para redefinição.",
                                Toast.LENGTH_LONG
                            ).show()
                            navController.popBackStack() // volta para a tela anterior (login)
                        },
                        onError = { msg ->
                            carregando = false
                            Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
        ) {
            if (carregando) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            } else {
                Text("Enviar link")
            }
        }
    }
}
