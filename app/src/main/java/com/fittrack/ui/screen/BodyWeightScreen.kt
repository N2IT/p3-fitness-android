package com.fittrack.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fittrack.ui.components.*
import com.fittrack.ui.theme.*
import com.fittrack.ui.viewmodel.BodyWeightUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BodyWeightScreen(
    uiState: BodyWeightUiState,
    onWeightChanged: (String) -> Unit,
    onLog: () -> Unit,
    onBack: () -> Unit
) {
    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Body Weight", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = TextPrimary
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Input card
                item {
                    FitTrackCard {
                        Text(
                            "Log Weight",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.weightInput,
                                onValueChange = { onWeightChanged(it.filter { c -> c.isDigit() || c == '.' }) },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Weight", color = TextTertiary) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricBlue,
                                    unfocusedBorderColor = DarkSurfaceVariant,
                                    cursorColor = ElectricBlue,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = DarkCard,
                                    unfocusedContainerColor = DarkCard
                                )
                            )
                            Button(
                                onClick = onLog,
                                enabled = uiState.weightInput.toDoubleOrNull() != null,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text("Log", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Chart placeholder
                if (uiState.weights.size >= 2) {
                    item {
                        FitTrackCard {
                            Text(
                                "Trend",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(8.dp))
                            val min = uiState.weights.minOf { it.weight }
                            val max = uiState.weights.maxOf { it.weight }
                            val range = max - min
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                uiState.weights.reversed().takeLast(30).forEach { bw ->
                                    val fraction = if (range > 0) ((bw.weight - min) / range).toFloat() else 1f
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(fraction.coerceIn(0.1f, 1f))
                                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                            .background(ElectricBlue)
                                    )
                                }
                            }
                        }
                    }
                }

                // History
                item {
                    Text(
                        "History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                items(uiState.weights) { bw ->
                    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            dateFormat.format(Date(bw.loggedAt)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            "${bw.weight}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}
