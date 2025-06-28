package com.example.unihub.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search // o ícone Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppColors {
    val bgSecondary = Color(0xFFEFEFEF)
    val iconTint = Color(0xFF757575)
}

@Composable
fun SearchBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )

            .fillMaxWidth()
            .height(42.dp)
            .background(
                color = AppColors.bgSecondary,
                shape = RoundedCornerShape(size = 8.dp)
            )
            .padding(start = 12.dp, end = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.matchParentSize()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Ícone de pesquisa",
                tint = AppColors.iconTint,
                modifier = Modifier
                    .padding(1.dp)
                    .width(24.dp)
                    .height(24.dp)
            )

            content()
        }
    }
}