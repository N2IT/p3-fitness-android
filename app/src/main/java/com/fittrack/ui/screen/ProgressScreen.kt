package com.fittrack.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fittrack.data.entity.Exercise
import com.fittrack.ui.components.*
import com.fittrack.ui.theme.*
import com.fittrack.ui.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressScreen(
    uiState: ProgressUiState,
    onExerciseSelected: (Exercise) -> Unit,
    onMetricChanged: (ChartMetric) -> Unit,
    onTimeRangeChanged: (TimeRange) -> Unit
) {
    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Progress", fontWeight = FontWeight.Bold) },
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
                // Exercise picker
                item {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedExercise?.name ?: "Select Exercise",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = DarkSurfaceVariant,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkCard,
                                unfocusedContainerColor = DarkCard
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            containerColor = DarkCard
                        ) {
                            uiState.exercises.forEach { exercise ->
                                DropdownMenuItem(
                                    text = {
                                        Text(exercise.name, color = TextPrimary)
                                    },
                                    onClick = {
                                        onExerciseSelected(exercise)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Metric toggle
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChartMetric.entries.forEach { metric ->
                            FilterChip(
                                selected = uiState.chartMetric == metric,
                                onClick = { onMetricChanged(metric) },
                                label = { Text(metric.label) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }

                // Time range
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(TimeRange.entries) { range ->
                            MuscleGroupChip(
                                name = range.label,
                                isSelected = uiState.timeRange == range,
                                onClick = { onTimeRangeChanged(range) }
                            )
                        }
                    }
                }

                // Chart area
                item {
                    FitTrackCard {
                        if (uiState.selectedExercise == null) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Select an exercise to view progress",
                                    color = TextTertiary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else if (uiState.dataPoints.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No data yet for this exercise",
                                    color = TextTertiary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            // Simple text-based chart summary
                            Column {
                                Text(
                                    "${uiState.chartMetric.label} Over Time",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(8.dp))

                                val min = uiState.dataPoints.minOf { it.value }
                                val max = uiState.dataPoints.maxOf { it.value }
                                val latest = uiState.dataPoints.last().value

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    StatColumn("${formatNum(min)}", "Min")
                                    StatColumn("${formatNum(max)}", "Max")
                                    StatColumn("${formatNum(latest)}", "Latest")
                                    StatColumn("${uiState.dataPoints.size}", "Sessions")
                                }

                                Spacer(Modifier.height(12.dp))

                                // Mini bar chart
                                if (uiState.dataPoints.size > 1) {
                                    val range = max - min
                                    val barHeight = 100.dp
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(barHeight),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        uiState.dataPoints.takeLast(20).forEach { point ->
                                            val fraction = if (range > 0) ((point.value - min) / range).toFloat() else 1f
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(fraction.coerceIn(0.05f, 1f))
                                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                    .background(ElectricBlue.copy(alpha = 0.5f + fraction * 0.5f))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // PR history
                if (uiState.prHistory.isNotEmpty()) {
                    item {
                        Text(
                            "PR History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    items(uiState.prHistory.take(10)) { pr ->
                        val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, "PR", tint = AccentOrange, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    pr.recordType.replace("_", " ").replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    dateFormat.format(Date(pr.achievedAt)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary
                                )
                            }
                            Text(
                                formatNum(pr.value),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentOrange
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ElectricBlue)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}

private fun formatNum(value: Double): String {
    return if (value == value.toLong().toDouble()) value.toLong().toString()
    else String.format("%.1f", value)
}
