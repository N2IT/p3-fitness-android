package com.fittrack.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittrack.ui.components.*
import com.fittrack.ui.theme.*
import com.fittrack.ui.viewmodel.ActiveWorkoutUiState
import com.fittrack.ui.viewmodel.InputMode
import com.fittrack.ui.viewmodel.WorkoutExerciseState
import com.fittrack.ui.viewmodel.WorkoutSet

@Composable
fun ActiveWorkoutScreen(
    uiState: ActiveWorkoutUiState,
    onUpdateWeight: (Int, Int, String) -> Unit,
    onUpdateReps: (Int, Int, String) -> Unit,
    onToggleSet: (Int, Int) -> Unit,
    onAddSet: (Int) -> Unit,
    onRemoveSet: (Int, Int) -> Unit,
    onSetActiveInput: (Int, Int, InputMode) -> Unit,
    onAppendInput: (String) -> Unit,
    onBackspaceInput: () -> Unit,
    onClearInput: () -> Unit,
    onDismissInput: () -> Unit,
    onFinish: () -> Unit,
    onDiscard: () -> Unit
) {
    var showDiscardDialog by remember { mutableStateOf(false) }

    GradientBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            WorkoutHeader(
                routineName = uiState.routineName,
                elapsed = uiState.elapsedSeconds,
                progress = if (uiState.totalSets > 0) {
                    uiState.completedSets.toFloat() / uiState.totalSets
                } else 0f,
                completedSets = uiState.completedSets,
                totalSets = uiState.totalSets
            )

            // Exercise list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(uiState.exercises) { exerciseIndex, exerciseState ->
                    ExerciseCard(
                        exerciseState = exerciseState,
                        exerciseIndex = exerciseIndex,
                        activeExerciseIndex = uiState.activeExerciseIndex,
                        activeSetIndex = uiState.activeSetIndex,
                        inputMode = uiState.inputMode,
                        onToggleSet = { setIndex -> onToggleSet(exerciseIndex, setIndex) },
                        onSetActiveInput = { setIndex, mode -> onSetActiveInput(exerciseIndex, setIndex, mode) },
                        onUpdateWeight = { setIndex, weight -> onUpdateWeight(exerciseIndex, setIndex, weight) },
                        onUpdateReps = { setIndex, reps -> onUpdateReps(exerciseIndex, setIndex, reps) },
                        onAddSet = { onAddSet(exerciseIndex) },
                        onRemoveSet = { setIndex -> onRemoveSet(exerciseIndex, setIndex) }
                    )
                }
                item { Spacer(Modifier.height(100.dp)) }
            }

            // Numeric keypad (when active)
            AnimatedVisibility(
                visible = uiState.inputMode != InputMode.NONE,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NumericKeypad(
                    onDigit = onAppendInput,
                    onBackspace = onBackspaceInput,
                    onClear = onClearInput,
                    onDone = onDismissInput,
                    showDecimal = uiState.inputMode == InputMode.WEIGHT
                )
            }

            // Bottom bar (when keypad hidden)
            AnimatedVisibility(
                visible = uiState.inputMode == InputMode.NONE,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                BottomBar(
                    onFinish = onFinish,
                    onDiscard = { showDiscardDialog = true },
                    isFinishing = uiState.isFinishing,
                    hasCompletedSets = uiState.completedSets > 0
                )
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard Workout?") },
            text = { Text("All progress for this session will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onDiscard()
                }) {
                    Text("Discard", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Going")
                }
            },
            containerColor = DarkCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    // PR celebration overlay
    AnimatedVisibility(
        visible = uiState.prCelebration != null,
        enter = fadeIn() + scaleIn(initialScale = 0.5f),
        exit = fadeOut() + scaleOut(targetScale = 1.5f)
    ) {
        uiState.prCelebration?.let { pr ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NEW PR!",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = AccentOrange
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = pr.exerciseName,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "${pr.recordType.replace("_", " ").replaceFirstChar { it.uppercase() }}: ${String.format("%.1f", pr.value)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ElectricBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutHeader(
    routineName: String,
    elapsed: Long,
    progress: Float,
    completedSets: Int,
    totalSets: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = routineName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatDuration(elapsed),
                style = MaterialTheme.typography.bodyLarge,
                color = ElectricBlue,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.width(16.dp))

        // Progress ring
        Box(contentAlignment = Alignment.Center) {
            SweepProgressRing(
                progress = progress,
                size = 52.dp,
                strokeWidth = 5.dp
            )
            Text(
                text = "$completedSets/$totalSets",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExerciseCard(
    exerciseState: WorkoutExerciseState,
    exerciseIndex: Int,
    activeExerciseIndex: Int,
    activeSetIndex: Int,
    inputMode: InputMode,
    onToggleSet: (Int) -> Unit,
    onSetActiveInput: (Int, InputMode) -> Unit,
    onUpdateWeight: (Int, String) -> Unit,
    onUpdateReps: (Int, String) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit
) {
    FitTrackCard {
        // Exercise name
        Text(
            text = exerciseState.exercise.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(Modifier.height(4.dp))

        Row {
            EquipmentBadge(exerciseState.exercise.equipment)
            Spacer(Modifier.width(8.dp))
            Text(
                exerciseState.exercise.muscleGroup,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }

        Spacer(Modifier.height(12.dp))

        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SET", style = MaterialTheme.typography.labelSmall, color = TextTertiary, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
            Text("PREVIOUS", style = MaterialTheme.typography.labelSmall, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Text("WEIGHT", style = MaterialTheme.typography.labelSmall, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Text("REPS", style = MaterialTheme.typography.labelSmall, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Spacer(Modifier.width(44.dp))
        }

        Spacer(Modifier.height(4.dp))

        // Set rows
        exerciseState.sets.forEachIndexed { setIndex, set ->
            SetRow(
                set = set,
                exerciseIndex = exerciseIndex,
                setIndex = setIndex,
                isWeightActive = activeExerciseIndex == exerciseIndex && activeSetIndex == setIndex && inputMode == InputMode.WEIGHT,
                isRepsActive = activeExerciseIndex == exerciseIndex && activeSetIndex == setIndex && inputMode == InputMode.REPS,
                onToggle = { onToggleSet(setIndex) },
                onWeightTap = { onSetActiveInput(setIndex, InputMode.WEIGHT) },
                onRepsTap = { onSetActiveInput(setIndex, InputMode.REPS) },
                onRemove = { onRemoveSet(setIndex) },
                canRemove = exerciseState.sets.size > 1
            )
        }

        Spacer(Modifier.height(8.dp))

        // Add set
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onAddSet)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, "Add set", tint = ElectricBlue, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Add Set", color = ElectricBlue, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SetRow(
    set: WorkoutSet,
    exerciseIndex: Int,
    setIndex: Int,
    isWeightActive: Boolean,
    isRepsActive: Boolean,
    onToggle: () -> Unit,
    onWeightTap: () -> Unit,
    onRepsTap: () -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    val borderColor by animateColorAsState(
        targetValue = if (set.isCompleted) SetCompletedBorder else Color.Transparent,
        label = "setBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (set.isCompleted) ElectricBlue.copy(alpha = 0.05f) else Color.Transparent,
        label = "setBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .border(
                width = if (set.isCompleted) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Set number
        Text(
            text = "${set.setNumber}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (set.isCompleted) ElectricBlue else TextSecondary,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.Center
        )

        // Previous
        Text(
            text = if (set.previousWeight != null && set.previousReps != null) {
                "${formatWeightDisplay(set.previousWeight)} × ${set.previousReps}"
            } else "—",
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        // Weight input
        InputCell(
            value = set.weight,
            placeholder = "0",
            isActive = isWeightActive,
            onClick = onWeightTap,
            modifier = Modifier.weight(1f)
        )

        // Reps input
        InputCell(
            value = set.reps,
            placeholder = "0",
            isActive = isRepsActive,
            onClick = onRepsTap,
            modifier = Modifier.weight(1f)
        )

        // Complete checkmark
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (set.isCompleted) ElectricBlue else DarkSurfaceVariant
                )
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                "Complete set",
                tint = if (set.isCompleted) Color.White else TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun InputCell(
    value: String,
    placeholder: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isActive) ElectricBlue else Color.Transparent,
        label = "inputBorder"
    )

    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.ifEmpty { placeholder },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (value.isEmpty()) TextTertiary else TextPrimary
        )
    }
}

@Composable
private fun BottomBar(
    onFinish: () -> Unit,
    onDiscard: () -> Unit,
    isFinishing: Boolean,
    hasCompletedSets: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onDiscard) {
            Text("Discard", color = TextSecondary)
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onFinish,
            enabled = hasCompletedSets && !isFinishing,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentOrange,
                disabledContainerColor = AccentOrange.copy(alpha = 0.3f)
            ),
            modifier = Modifier.height(48.dp)
        ) {
            Text(
                if (isFinishing) "Saving..." else "Finish Workout",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

private fun formatWeightDisplay(weight: Double): String {
    return if (weight == weight.toLong().toDouble()) {
        weight.toLong().toString()
    } else {
        weight.toString()
    }
}
