package com.fittrack.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fittrack.ui.components.*
import com.fittrack.ui.theme.*
import com.fittrack.ui.viewmodel.HistoryUiState
import com.fittrack.ui.viewmodel.WorkoutSession
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onSessionClick: (String) -> Unit,
    onDeleteSession: (String) -> Unit
) {
    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("History", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = TextPrimary
                    )
                )
            }
        ) { padding ->
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ElectricBlue)
                }
            } else if (uiState.sessions.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    EmptyStateMessage(
                        title = "No Workouts Yet",
                        subtitle = "Complete a workout to see your history"
                    )
                }
            } else {
                // Group by week
                val grouped = uiState.sessions.groupBy { session ->
                    val cal = Calendar.getInstance().apply { timeInMillis = session.date }
                    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                    val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
                    "Week of ${fmt.format(cal.time)}"
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grouped.forEach { (week, sessions) ->
                        item {
                            Text(
                                text = week,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextTertiary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(sessions, key = { it.sessionId }) { session ->
                            SessionCard(
                                session = session,
                                onClick = { onSessionClick(session.sessionId) },
                                onDelete = { onDeleteSession(session.sessionId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionCard(
    session: WorkoutSession,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("EEE, MMM d · h:mm a", Locale.getDefault()) }

    FitTrackCard(onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Gradient left border
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(listOf(ElectricBlue, AccentOrange))
                    )
            )
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.routineName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = dateFormat.format(Date(session.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatChip("${formatDuration(session.durationSeconds)}", "Duration")
                    StatChip("${session.setCount}", "Sets")
                    StatChip("${formatVolume(session.totalVolume)}", "Volume")
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, "Delete", tint = TextTertiary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun StatChip(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ElectricBlue)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}

private fun formatVolume(volume: Double): String {
    return when {
        volume >= 1000 -> "${String.format("%.1f", volume / 1000)}k"
        else -> volume.toInt().toString()
    }
}
