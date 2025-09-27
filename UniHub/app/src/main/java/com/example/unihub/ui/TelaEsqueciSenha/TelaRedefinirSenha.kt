package com.example.unihub.ui.TelaEsqueciSenha

import android.widget.Toast
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
import com.example.unihub.Screen
import com.example.unihub.data.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun TelaRedefinirSenha(
    token: String,
    navController: NavController,
    repository: AuthRepository = AuthRepository()
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var senha by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Definir nova senha",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            singleLine = true,
            label = { Text("Nova senha (mín. 6)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmar,
            onValueChange = { confirmar = it },
            singleLine = true,
            label = { Text("Confirmar nova senha") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            enabled = !carregando && senha.length >= 6 && senha == confirmar,
            onClick = {
                if (senha != confirmar) {
                    Toast.makeText(ctx, "As senhas não coincidem.", Toast.LENGTH_LONG).show()
                    return@Button
                }
                carregando = true
                scope.launch {
                    repository.redefinirSenha(
                        token = token,
                        novaSenha = senha,
                        onSuccess = {
                            carregando = false
                            Toast.makeText(ctx, "Senha alterada!", Toast.LENGTH_LONG).show()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
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
                Text("Salvar nova senha")
            }
        }
    }
}
