package com.example.unihub.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CabecalhoPrincipal(
    titulo: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top= 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = titulo,
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 25.sp,
                fontWeight = FontWeight.SemiBold,
            )
        )
    }
}