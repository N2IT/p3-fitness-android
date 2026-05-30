package com.fittrack.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fittrack.data.entity.WorkoutRoutine
import com.fittrack.ui.components.*
import com.fittrack.ui.theme.*
import com.fittrack.ui.viewmodel.RoutineListUiState
import androidx.compose.material.icons.filled.Schedule

@Composable
fun RoutineListScreen(
    uiState: RoutineListUiState,
    onCreateRoutine: () -> Unit,
    onEditRoutine: (Int) -> Unit,
    onStartWorkout: (Int) -> Unit,
    onDeleteRoutine: (WorkoutRoutine) -> Unit
) {
    var deleteTarget by remember { mutableStateOf<WorkoutRoutine?>(null) }

    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            "My Routines",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = TextPrimary
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onCreateRoutine,
                    containerColor = ElectricBlue,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Routine")
                }
            }
        ) { padding ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ElectricBlue)
                }
            } else if (uiState.routines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateMessage(
                        title = "No Routines Yet",
                        subtitle = "Tap + to create your first workout routine"
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        items = uiState.routines,
                        key = { _, routine -> routine.id }
                    ) { index, routine ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { visible = true }

                        val alpha by animateFloatAsState(
                            targetValue = if (visible) 1f else 0f,
                            animationSpec = tween(
                                durationMillis = 400,
                                delayMillis = index * 80,
                                easing = FastOutSlowInEasing
                            ),
                            label = "itemAlpha"
                        )
                        val offsetY by animateIntAsState(
                            targetValue = if (visible) 0 else 40,
                            animationSpec = tween(
                                durationMillis = 400,
                                delayMillis = index * 80,
                                easing = FastOutSlowInEasing
                            ),
                            label = "itemOffset"
                        )

                        Box(
                            modifier = Modifier
                                .alpha(alpha)
                                .offset(y = offsetY.dp)
                        ) {
                            RoutineCard(
                                routine = routine,
                                exerciseCount = uiState.routineExerciseCounts[routine.id] ?: 0,
                                exerciseNames = uiState.routineExerciseNames[routine.id] ?: emptyList(),
                                estimatedMinutes = uiState.routineEstimatedMinutes[routine.id] ?: 0,
                                onEdit = { onEditRoutine(routine.id) },
                                onStart = { onStartWorkout(routine.id) },
                                onDelete = { deleteTarget = routine }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Delete confirmation dialog
    deleteTarget?.let { routine ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Routine") },
            text = { Text("Delete \"${routine.name}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteRoutine(routine)
                    deleteTarget = null
                }) {
                    Text("Delete", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}

@Composable
private fun RoutineCard(
    routine: WorkoutRoutine,
    exerciseCount: Int,
    exerciseNames: List<String>,
    estimatedMinutes: Int,
    onEdit: () -> Unit,
    onStart: () -> Unit,
    onDelete: () -> Unit
) {
    FitTrackCard(onClick = onEdit) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left accent
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ElectricBlue)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$exerciseCount exercise${if (exerciseCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    if (estimatedMinutes > 0) {
                        Text("·", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "~${estimatedMinutes} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }
                if (exerciseNames.isNotEmpty()) {
                    Text(
                        text = exerciseNames.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (routine.timesPerformed > 0) {
                    Text(
                        text = "Performed ${routine.timesPerformed}x",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricBlue.copy(alpha = 0.7f)
                    )
                }
            }

            // Actions
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onStart,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ElectricBlue)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Start Workout",
                    tint = Color.White
                )
            }
        }
    }
}
