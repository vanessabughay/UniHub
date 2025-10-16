package com.example.unihub.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import com.example.unihub.R
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun BotoesFormulario(
    onConfirm: () -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = if (onDelete != null) Arrangement.spacedBy(12.dp) else Arrangement.End, // Ajusta o arranjo
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        onDelete?.let { deleteAction ->
            OutlinedButton(
                onClick = deleteAction, // Usa a ação de deletar fornecida
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                border = BorderStroke(1.dp, Color.Transparent),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFFF5D9DA),
                    contentColor = Color.Black.copy(alpha = 0.63f)
                )
            ) {
                Text(
                    text = "Excluir", // Ou deleteButtonText
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp,
                        color = Color.Black.copy(alpha = 0.63f)
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_trash), // Certifique-se que este ícone existe
                    contentDescription = "Excluir",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Unspecified
                )
            }
        }

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .weight(1f) // Se onDelete for null, este botão pegará mais espaço ou todo o espaço
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF90DC91), // Verde claro para confirmar
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "Confirmar", // Ou confirmButtonText
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 24.sp,
                    color = Color.Black
                )
            )
        }
    }
}