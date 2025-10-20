package com.example.unihub.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Cabeçalho para telas secundárias, com um botão de "voltar" obrigatório
 * e um ícone opcional à direita.
 *
 * @param titulo O título da tela.
 * @param onVoltar Ação a ser executada ao clicar no botão de voltar.
 * @param onIconeDireitaClick Ação opcional para o ícone da direita. Se for nulo, o ícone não aparece.
 */

@Composable
fun CabecalhoAlternativo(
    titulo: String,
    onVoltar: () -> Unit,
    onIconeDireitaClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top=25.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botão de Voltar (à esquerda)
            IconButton(onClick = onVoltar) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Voltar",
                    modifier = Modifier.size(25.dp)
                )
            }

            // Botão Opcional (à direita)
            if (onIconeDireitaClick != null) {
                IconButton(onClick = onIconeDireitaClick) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notificações"
                    )
                }
            }
        }

        // Texto do Título (centralizado)
        Text(
            text = titulo,
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
            )
        )
    }
}