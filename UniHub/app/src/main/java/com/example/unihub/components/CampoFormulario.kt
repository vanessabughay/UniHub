package com.example.unihub.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CampoFormulario(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth() // Ocupa a largura total disponível
            .background(Color(0xE9CFE5D0), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp) // padding interno do Box
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            // Para controlar o espaçamento entre o label e o campo de texto
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${label.uppercase()}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                ),
                modifier = Modifier
                    .weight(0.3f) // Ocupa 30% do espaço disponível na linha
            )

            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    val sanitizedValue = if (singleLine) {
                        newValue.replace("\n", " ").replace("\r", "")
                    } else {
                        newValue
                    }
                    onValueChange(sanitizedValue)
                },
                textStyle = TextStyle(
                    color = Color.Black.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 19.6.sp,
                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily
                ),
                modifier = Modifier
                    .weight(0.7f), // Ocupa 70% do espaço restante
                singleLine = singleLine,
                maxLines = if (singleLine) 1 else Int.MAX_VALUE
            )
        }
    }
}