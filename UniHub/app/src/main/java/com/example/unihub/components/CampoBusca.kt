package com.example.unihub.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CampoBusca(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val lightGrayBackground = Color(0xFFFFFFFF) // Cinza bem claro
    val mediumGrayBorder = Color(0xFFBDBDBD)    // Cinza médio

    SearchBox(
        modifier = modifier
            .clip(shape)
            .background(lightGrayBackground, shape)
            .border(1.dp, mediumGrayBorder, shape)
    ) {
        val textColor = MaterialTheme.colorScheme.onSurface
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            singleLine = true,
            textStyle = TextStyle(
                color = textColor,
                fontSize = 16.sp
            ),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFF4D4D4D),
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}