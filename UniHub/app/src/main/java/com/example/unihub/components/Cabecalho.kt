package com.example.unihub.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CabecalhoAlternativo(
    titulo: String,
    onVoltar: () -> Unit,
    habilitarVoltar: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 25.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onVoltar,
                enabled = habilitarVoltar
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Voltar",
                    modifier = Modifier.size(25.dp)
                )
            }
        }


        Text(
            text = titulo,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        val menuActions = LocalMenuActions.current
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (menuActions.enabled) {
                IconButton(onClick = menuActions.openMenu) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = "Abrir menu",
                        tint = Color(0xFF274B6F),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}