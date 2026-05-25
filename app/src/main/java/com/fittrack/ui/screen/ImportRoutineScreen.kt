package com.fittrack.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fittrack.data.api.ImportState
import com.fittrack.data.api.ImportedRoutine
import com.fittrack.ui.components.*
import com.fittrack.ui.theme.*
import com.fittrack.ui.viewmodel.ImportMode
import com.fittrack.ui.viewmodel.ImportUiState

@Composable
fun ImportRoutineScreen(
    uiState: ImportUiState,
    onModeChanged: (ImportMode) -> Unit,
    onUrlChanged: (String) -> Unit,
    onTextChanged: (String) -> Unit,
    onRoutineNameChanged: (String) -> Unit,
    onStartImport: () -> Unit,
    onSaveRoutine: (ImportedRoutine) -> Unit,
    onReset: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("AI Tools", fontWeight = FontWeight.Bold) },
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
                // Mode toggle
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        listOf(ImportMode.URL to "Import URL", ImportMode.TEXT to "Describe Workout").forEach { (mode, label) ->
                            val isSelected = uiState.mode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ElectricBlue else Color.Transparent)
                                    .clickable { onModeChanged(mode) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Provider indicator
                item {
                    if (!uiState.hasApiKey) {
                        FitTrackCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, "Warning", tint = WarningYellow, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("API Key Required", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text(
                                        "Add an API key for any supported AI provider in Settings",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                TextButton(onClick = onNavigateToSettings) {
                                    Text("Settings", color = ElectricBlue)
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, "AI", tint = AccentOrange, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Using ${uiState.providerName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                            }
                            TextButton(onClick = onNavigateToSettings) {
                                Text("Change", color = ElectricBlue, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                // Input area
                item {
                    when (uiState.mode) {
                        ImportMode.URL -> {
                            OutlinedTextField(
                                value = uiState.urlInput,
                                onValueChange = onUrlChanged,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Paste workout URL...", color = TextTertiary) },
                                leadingIcon = { Icon(Icons.Default.Link, "URL", tint = TextTertiary) },
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
                        }
                        ImportMode.TEXT -> {
                            OutlinedTextField(
                                value = uiState.textInput,
                                onValueChange = onTextChanged,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp),
                                placeholder = {
                                    Text(
                                        "Describe your workout...\ne.g., \"Push day with bench press, overhead press, and tricep work\"",
                                        color = TextTertiary
                                    )
                                },
                                minLines = 4,
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
                        }
                    }
                }

                // Action button
                item {
                    val canImport = when (uiState.mode) {
                        ImportMode.URL -> uiState.urlInput.isNotBlank()
                        ImportMode.TEXT -> uiState.textInput.isNotBlank()
                    } && uiState.importState !is ImportState.Loading

                    ElectricButton(
                        text = when (uiState.importState) {
                            is ImportState.Loading -> "Generating..."
                            else -> if (uiState.mode == ImportMode.URL) "Import" else "Generate"
                        },
                        onClick = onStartImport,
                        enabled = canImport
                    )
                }

                // Loading animation
                if (uiState.importState is ImportState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = ElectricBlue)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "AI is designing your workout...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // Error
                if (uiState.importState is ImportState.Error) {
                    item {
                        FitTrackCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, "Error", tint = ErrorRed)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    (uiState.importState as ImportState.Error).message,
                                    color = ErrorRed,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Preview
                if (uiState.importState is ImportState.Preview) {
                    val routine = (uiState.importState as ImportState.Preview).routine

                    item {
                        Text(
                            "Preview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = uiState.editedRoutineName,
                            onValueChange = onRoutineNameChanged,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Routine Name", color = TextTertiary) },
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
                    }

                    itemsIndexed(routine.exercises) { _, exercise ->
                        FitTrackCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        exercise.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (exercise.muscleGroup.isNotBlank()) {
                                            Text(exercise.muscleGroup, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                        }
                                        if (exercise.equipment.isNotBlank()) {
                                            EquipmentBadge(exercise.equipment)
                                        }
                                    }
                                }
                                Text(
                                    "${exercise.sets} × ${exercise.reps}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue
                                )
                            }
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = onReset,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Discard", color = TextSecondary)
                            }
                            Button(
                                onClick = { onSaveRoutine(routine) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                            ) {
                                Text("Save Routine", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Success
                if (uiState.importState is ImportState.Success) {
                    item {
                        FitTrackCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, "Success", tint = SuccessGreen)
                                Spacer(Modifier.width(8.dp))
                                Text("Routine saved!", color = SuccessGreen, style = MaterialTheme.typography.bodyLarge)
                            }
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = onReset) {
                                Text("Import Another", color = ElectricBlue)
                            }
                        }
                    }
                }
            }
        }
    }
}
