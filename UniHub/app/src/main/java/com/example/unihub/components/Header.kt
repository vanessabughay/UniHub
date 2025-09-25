package com.example.unihub.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun Header(
    titulo: String,
    onVoltar: () -> Unit,
    onClickIconeDireita: (() -> Unit)? = null
) {
    var iconeAtivo by remember { mutableStateOf(false) } // Estado para alternar o ícone de notificação

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botão Voltar (sempre presente)
            IconButton(onClick = onVoltar) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Voltar",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp)
                )
            }

            // ---- ÍCONE DE NOTIFICAÇÕES CONDICIONAL ----
            // Só mostra o IconButton se onClickIconeDireita não for null
            onClickIconeDireita?.let { acaoIconeDireita ->
                IconButton(
                    onClick = {
                        iconeAtivo = !iconeAtivo // Alterna o estado local do ícone
                        acaoIconeDireita.invoke() // Chama a ação passada
                    }
                ) {
                    Icon(
                        imageVector = if (iconeAtivo) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                        contentDescription = "Notificações",
                        tint = if (iconeAtivo)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Texto do Título (centralizado no Box)
        Text(
            text = titulo,
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 22.4.sp,
                letterSpacing = (-0.32).sp
            )
        )
    }
}