package com.example.unihub.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    modifier: Modifier = Modifier,
    placeholder: String = "Selecionar data"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            //.background(Color(0xE9CFE5D0), shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp), // padding uniforme
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                //color = Color.Black
            )
        )

        Text(
            text = value.ifBlank { placeholder },
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 19.6.sp,
                color = if (value.isBlank()) Color.Black.copy(alpha = 0.35f)
                else Color.Black.copy(alpha = 0.7f)
            )
        )
    }
}
