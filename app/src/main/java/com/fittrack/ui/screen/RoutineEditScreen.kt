package com.fittrack.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fittrack.ui.components.*
import com.fittrack.ui.theme.*
import com.fittrack.ui.viewmodel.RoutineEditUiState
import com.fittrack.ui.viewmodel.RoutineExerciseWithDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditScreen(
    uiState: RoutineEditUiState,
    onNameChanged: (String) -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onMoveExercise: (Int, Int) -> Unit,
    onUpdateSetsReps: (Int, Int, Int) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onSearchChanged: (String) -> Unit,
    onMuscleGroupSelected: (String?) -> Unit,
    onExercisePicked: (com.fittrack.data.entity.Exercise) -> Unit,
    onDismissExercisePicker: () -> Unit
) {
    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (uiState.routine != null) "Edit Routine" else "New Routine",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = onSave,
                            enabled = uiState.routineName.isNotBlank() && !uiState.isSaving
                        ) {
                            Text(
                                "Save",
                                color = if (uiState.routineName.isNotBlank()) ElectricBlue else TextTertiary,
                                fontWeight = FontWeight.Bold
                            )
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Routine name
                item {
                    OutlinedTextField(
                        value = uiState.routineName,
                        onValueChange = onNameChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Routine Name", color = TextTertiary) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
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
                }

                // Exercise list
                itemsIndexed(
                    items = uiState.exercises,
                    key = { i, item -> "${item.exercise.id}_$i" }
                ) { index, item ->
                    ExerciseEditCard(
                        item = item,
                        index = index,
                        canMoveUp = index > 0,
                        canMoveDown = index < uiState.exercises.size - 1,
                        onRemove = { onRemoveExercise(index) },
                        onMoveUp = { onMoveExercise(index, index - 1) },
                        onMoveDown = { onMoveExercise(index, index + 1) },
                        onSetsChanged = { sets -> onUpdateSetsReps(index, sets, item.routineExercise.defaultReps) },
                        onRepsChanged = { reps -> onUpdateSetsReps(index, item.routineExercise.defaultSets, reps) }
                    )
                }

                // Add exercise button
                item {
                    OutlinedButton(
                        onClick = onAddExercise,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(ElectricBlue.copy(alpha = 0.3f))
                        )
                    ) {
                        Icon(Icons.Default.Add, "Add", tint = ElectricBlue)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Exercise", color = ElectricBlue)
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    // Exercise picker bottom sheet
    if (uiState.showExercisePicker) {
        ModalBottomSheet(
            onDismissRequest = onDismissExercisePicker,
            containerColor = DarkSurface,
            contentColor = TextPrimary
        ) {
            ExercisePickerContent(
                exercises = uiState.filteredExercises,
                muscleGroups = uiState.muscleGroups,
                searchQuery = uiState.searchQuery,
                selectedMuscleGroup = uiState.selectedMuscleGroup,
                onSearchChanged = onSearchChanged,
                onMuscleGroupSelected = onMuscleGroupSelected,
                onExercisePicked = onExercisePicked
            )
        }
    }
}

@Composable
private fun ExerciseEditCard(
    item: RoutineExerciseWithDetails,
    index: Int,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onSetsChanged: (Int) -> Unit,
    onRepsChanged: (Int) -> Unit
) {
    FitTrackCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reorder buttons
            Column {
                IconButton(
                    onClick = onMoveUp,
                    enabled = canMoveUp,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp, "Move up",
                        tint = if (canMoveUp) TextSecondary else TextTertiary.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = canMoveDown,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown, "Move down",
                        tint = if (canMoveDown) TextSecondary else TextTertiary.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close, "Remove",
                            tint = ErrorRed.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    EquipmentBadge(item.exercise.equipment)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        item.exercise.muscleGroup,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Sets/Reps steppers
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StepperField(
                        label = "Sets",
                        value = item.routineExercise.defaultSets,
                        onValueChange = onSetsChanged,
                        min = 1, max = 10
                    )
                    StepperField(
                        label = "Reps",
                        value = item.routineExercise.defaultReps,
                        onValueChange = onRepsChanged,
                        min = 1, max = 30
                    )
                }
            }
        }
    }
}

@Composable
private fun StepperField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int,
    max: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.width(36.dp)
        )
        IconButton(
            onClick = { if (value > min) onValueChange(value - 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Remove, "Decrease",
                tint = if (value > min) ElectricBlue else TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = "$value",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.width(28.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IconButton(
            onClick = { if (value < max) onValueChange(value + 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Add, "Increase",
                tint = if (value < max) ElectricBlue else TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ExercisePickerContent(
    exercises: List<com.fittrack.data.entity.Exercise>,
    muscleGroups: List<String>,
    searchQuery: String,
    selectedMuscleGroup: String?,
    onSearchChanged: (String) -> Unit,
    onMuscleGroupSelected: (String?) -> Unit,
    onExercisePicked: (com.fittrack.data.entity.Exercise) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 500.dp)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Add Exercise",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search exercises...", color = TextTertiary) },
            leadingIcon = { Icon(Icons.Default.Search, "Search", tint = TextTertiary) },
            singleLine = true,
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

        Spacer(Modifier.height(12.dp))

        // Muscle group chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(muscleGroups) { group ->
                MuscleGroupChip(
                    name = group,
                    isSelected = group == selectedMuscleGroup,
                    onClick = { onMuscleGroupSelected(group) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Exercise list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(exercises, key = { it.id }) { exercise ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onExercisePicked(exercise) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            exercise.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                        Text(
                            exercise.muscleGroup,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                    EquipmentBadge(exercise.equipment)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
