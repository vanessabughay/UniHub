package com.example.unihub.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CampoCombobox(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    optionToDisplayedString: (T) -> String = { it.toString() },
    placeholder: String = "Selecione...",
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        // Removido o width(360.dp) fixo e usado fillMaxWidth()
        // As margens laterais agora são controladas pelo padding horizontal
        modifier = modifier
            .fillMaxWidth() // Ocupa a largura total disponível
            .background(Color(0xE9CFE5D0), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp) // Padding interno consistente
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                ),
                modifier = Modifier.weight(0.3f)
            )

            ExposedDropdownMenuBox(
                expanded = expanded && enabled,
                onExpandedChange = { if (enabled) expanded = !expanded },
                modifier = Modifier.weight(0.7f)
            ) {
                TextField(
                    value = selectedOption?.let { optionToDisplayedString(it) } ?: placeholder,
                    onValueChange = { /* Campo é somente leitura */ },
                    readOnly = true,
                    enabled = enabled,
                    textStyle = TextStyle(
                        color = if (selectedOption != null && enabled) Color.Black.copy(alpha = 0.7f)
                        else if (enabled) Color.Black.copy(alpha = 0.4f)
                        else Color.Black.copy(alpha = 0.3f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 19.6.sp,
                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily
                    ),
                    trailingIcon = {
                        if (enabled) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded && enabled,
                    onDismissRequest = { expanded = false }
                ) {
                    if (options.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Nenhuma opção") },
                            onClick = { expanded = false },
                            enabled = false
                        )
                    } else {
                        options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(optionToDisplayedString(option)) },
                                onClick = {
                                    onOptionSelected(option)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}