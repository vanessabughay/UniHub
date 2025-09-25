package com.example.unihub.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CampoData(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(

        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xE9CFE5D0), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp) // Padding consistente: 16.dp de cada lado
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium, // Usando FontWeight.Medium para consistência
                    lineHeight = 20.sp,
                    color = Color.Black
                ),
                modifier = Modifier.weight(0.3f) // Ocupa 30% do espaço da Row
            )

            Box(
                modifier = Modifier
                    .weight(0.7f) // Ocupa 70% do espaço restante na Row
                    .clickable { onClick() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterEnd // Alinha o conteúdo do Box (o Text) à direita
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 19.6.sp,
                        color = Color.Black.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}