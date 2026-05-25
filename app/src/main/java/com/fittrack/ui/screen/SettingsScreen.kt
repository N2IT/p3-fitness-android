package com.fittrack.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.fittrack.ui.components.*
import com.fittrack.ui.theme.*
import com.fittrack.ui.viewmodel.SettingsUiState

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onToggleUnit: () -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onSaveApiKey: () -> Unit,
    onClearApiKey: () -> Unit,
    onExportData: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToBodyWeight: () -> Unit
) {
    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Profile", fontWeight = FontWeight.Bold) },
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
                // Profile section
                item {
                    FitTrackCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, "Profile", tint = ElectricBlue, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    uiState.username,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    "${uiState.totalWorkouts} workouts completed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // Quick links
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onNavigateToAchievements,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkCard)
                        ) {
                            Icon(Icons.Default.EmojiEvents, "Achievements", tint = AccentOrange)
                            Spacer(Modifier.width(8.dp))
                            Text("Achievements", color = TextPrimary)
                        }
                        Button(
                            onClick = onNavigateToBodyWeight,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkCard)
                        ) {
                            Icon(Icons.Default.MonitorWeight, "Weight", tint = ElectricBlue)
                            Spacer(Modifier.width(8.dp))
                            Text("Body Weight", color = TextPrimary)
                        }
                    }
                }

                // Units
                item {
                    SectionHeader("Units")
                    FitTrackCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Weight Unit", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                            UnitToggle(selectedUnit = uiState.unitPreference, onToggle = onToggleUnit)
                        }
                    }
                }

                // AI Features
                item {
                    SectionHeader("AI Features")
                    FitTrackCard {
                        Text("Claude API Key", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        var showKey by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = uiState.apiKey,
                            onValueChange = onApiKeyChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("sk-ant-...", color = TextTertiary) },
                            singleLine = true,
                            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showKey = !showKey }) {
                                    Icon(
                                        if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        "Toggle visibility",
                                        tint = TextTertiary
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = DarkSurfaceVariant,
                                cursorColor = ElectricBlue,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onSaveApiKey,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                            ) {
                                Text("Save Key", fontWeight = FontWeight.Bold)
                            }
                            if (uiState.hasApiKey) {
                                OutlinedButton(
                                    onClick = onClearApiKey,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Remove", color = ErrorRed)
                                }
                            }
                        }
                        uiState.apiKeyTestResult?.let { result ->
                            Spacer(Modifier.height(4.dp))
                            Text(result, style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                        }
                    }
                }

                // Data
                item {
                    SectionHeader("Data")
                    FitTrackCard {
                        Button(
                            onClick = onExportData,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                        ) {
                            Icon(Icons.Default.Download, "Export", tint = ElectricBlue)
                            Spacer(Modifier.width(8.dp))
                            Text("Export Data (CSV)", color = TextPrimary)
                        }
                        uiState.exportStatus?.let { status ->
                            Spacer(Modifier.height(4.dp))
                            Text(status, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }

                // About
                item {
                    SectionHeader("About")
                    FitTrackCard {
                        Text("FitTrack v1.0", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                        Text("Built with Jetpack Compose", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = TextTertiary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}
