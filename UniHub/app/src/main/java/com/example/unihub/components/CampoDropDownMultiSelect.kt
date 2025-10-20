package com.example.unihub.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unihub.ui.ManterTarefa.ResponsavelOption
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoDropdownMultiSelect(
    label: String,
    options: List<ResponsavelOption>,
    selectedIds: Set<Long>,
    onSelectionChange: (Set<Long>) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)

    val selectedNames = remember(options, selectedIds) {
        options.filter { selectedIds.contains(it.id) }.map { it.nome }
    }

    val summaryText = remember(selectedNames, placeholder) {
        when {
            selectedNames.isEmpty() -> placeholder
            selectedNames.size <= 2 -> selectedNames.joinToString(", ")
            else -> {
                val prefix = selectedNames.take(2).joinToString(", ")
                "$prefix +${selectedNames.size - 2}"
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                ),
                modifier = Modifier.weight(0.35f)
            )

            ExposedDropdownMenuBox(
                expanded = expanded && enabled,
                onExpandedChange = {
                    if (enabled) {
                        expanded = !expanded
                    }
                },
                modifier = Modifier.weight(0.65f)
            ) {
                TextField(
                    value = summaryText,
                    onValueChange = {},
                    readOnly = true,
                    enabled = enabled,
                    textStyle = TextStyle(
                        color = if (selectedNames.isNotEmpty() && enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else if (enabled) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        },
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
                        errorIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
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
                            text = { Text(placeholder) },
                            enabled = false,
                            onClick = { expanded = false }
                        )
                    } else {
                        options.forEach { option ->
                            val isSelected = selectedIds.contains(option.id)
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = option.nome,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                },
                                onClick = {
                                    val updated = selectedIds.toMutableSet()
                                    if (!updated.add(option.id)) {
                                        updated.remove(option.id)
                                    }
                                    onSelectionChange(updated)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}