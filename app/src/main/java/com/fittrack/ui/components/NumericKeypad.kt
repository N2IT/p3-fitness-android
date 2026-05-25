package com.fittrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittrack.ui.theme.*

@Composable
fun NumericKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit,
    showDecimal: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Row 1: 1-2-3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeypadButton("1", Modifier.weight(1f)) { onDigit("1") }
            KeypadButton("2", Modifier.weight(1f)) { onDigit("2") }
            KeypadButton("3", Modifier.weight(1f)) { onDigit("3") }
        }
        // Row 2: 4-5-6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeypadButton("4", Modifier.weight(1f)) { onDigit("4") }
            KeypadButton("5", Modifier.weight(1f)) { onDigit("5") }
            KeypadButton("6", Modifier.weight(1f)) { onDigit("6") }
        }
        // Row 3: 7-8-9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeypadButton("7", Modifier.weight(1f)) { onDigit("7") }
            KeypadButton("8", Modifier.weight(1f)) { onDigit("8") }
            KeypadButton("9", Modifier.weight(1f)) { onDigit("9") }
        }
        // Row 4: ./C - 0 - Backspace/Done
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showDecimal) {
                KeypadButton(".", Modifier.weight(1f)) { onDigit(".") }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .clickable(onClick = onClear),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = ErrorRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            KeypadButton("0", Modifier.weight(1f)) { onDigit("0") }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .clickable(onClick = onBackspace),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElectricBlue)
                        .clickable(onClick = onDone),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ElectricBlue),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}
