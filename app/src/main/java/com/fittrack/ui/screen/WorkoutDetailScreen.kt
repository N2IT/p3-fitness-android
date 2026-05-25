package com.fittrack.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fittrack.data.entity.ExerciseLog
import com.fittrack.ui.components.*
import com.fittrack.ui.theme.*
import com.fittrack.ui.viewmodel.WorkoutDetailUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorkoutDetailScreen(
    uiState: WorkoutDetailUiState,
    onBack: () -> Unit
) {
    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Workout Details", fontWeight = FontWeight.Bold) },
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
            if (uiState.isLoading || uiState.detail == null) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ElectricBlue)
                }
            } else {
                val detail = uiState.detail
                val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d · h:mm a", Locale.getDefault()) }
                val prSetKeys = detail.sessionPrs.map { "${it.exerciseId}_${it.weight}_${it.reps}" }.toSet()

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Summary card
                    item {
                        FitTrackCard {
                            Text(
                                detail.session.routineName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                dateFormat.format(Date(detail.session.date)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                SummaryStatColumn(formatDuration(detail.session.durationSeconds), "Duration")
                                SummaryStatColumn("${detail.session.setCount}", "Sets")
                                SummaryStatColumn("${detail.session.exerciseCount}", "Exercises")
                                SummaryStatColumn(formatVolumeShort(detail.session.totalVolume), "Volume")
                            }
                        }
                    }

                    // Per-exercise breakdown
                    detail.exerciseLogs.forEach { (exerciseName, logs) ->
                        item {
                            FitTrackCard {
                                Text(
                                    exerciseName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(8.dp))

                                // Header
                                Row(Modifier.fillMaxWidth()) {
                                    Text("SET", style = MaterialTheme.typography.labelSmall, color = TextTertiary, modifier = Modifier.width(40.dp))
                                    Text("WEIGHT", style = MaterialTheme.typography.labelSmall, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                    Text("REPS", style = MaterialTheme.typography.labelSmall, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                    Spacer(Modifier.width(28.dp))
                                }

                                Spacer(Modifier.height(4.dp))

                                logs.forEach { log ->
                                    val isPr = prSetKeys.contains("${log.exerciseId}_${log.weight}_${log.reps}")
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "${log.setNumber}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextSecondary,
                                            modifier = Modifier.width(40.dp)
                                        )
                                        Text(
                                            formatWeightVal(log.weight),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            "${log.reps}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center
                                        )
                                        if (isPr) {
                                            Icon(
                                                Icons.Default.Star,
                                                "PR",
                                                tint = AccentOrange,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else {
                                            Spacer(Modifier.width(28.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ElectricBlue)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}

private fun formatVolumeShort(volume: Double): String {
    return when {
        volume >= 1000 -> "${String.format("%.1f", volume / 1000)}k"
        else -> volume.toInt().toString()
    }
}

private fun formatWeightVal(weight: Double): String {
    return if (weight == weight.toLong().toDouble()) weight.toLong().toString() else weight.toString()
}
