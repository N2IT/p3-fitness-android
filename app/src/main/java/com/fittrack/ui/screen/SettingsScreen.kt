package com.fittrack.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fittrack.data.api.LlmProvider
import com.fittrack.ui.components.*
import com.fittrack.ui.theme.*
import com.fittrack.ui.viewmodel.SettingsUiState

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onToggleUnit: () -> Unit,
    onSelectProvider: (LlmProvider) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onSaveApiKey: () -> Unit,
    onClearApiKey: () -> Unit,
    onExportData: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToBodyWeight: () -> Unit,
    onLogout: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

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
                    var showSignOutDialog by remember { mutableStateOf(false) }

                    FitTrackCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, "Profile", tint = ElectricBlue, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
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
                            TextButton(onClick = { showSignOutDialog = true }) {
                                Icon(
                                    Icons.Default.Logout,
                                    contentDescription = "Sign Out",
                                    tint = TextTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Sign Out", color = TextTertiary, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    if (showSignOutDialog) {
                        AlertDialog(
                            onDismissRequest = { showSignOutDialog = false },
                            title = { Text("Sign Out?") },
                            text = { Text("Your data will be saved. Sign back in with the same username to access it.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showSignOutDialog = false
                                    onLogout()
                                }) {
                                    Text("Sign Out", color = ErrorRed)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showSignOutDialog = false }) {
                                    Text("Cancel")
                                }
                            },
                            containerColor = DarkCard,
                            titleContentColor = TextPrimary,
                            textContentColor = TextSecondary
                        )
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

                // AI Provider Selection
                item {
                    SectionHeader("AI Provider")
                }

                items(LlmProvider.entries) { provider ->
                    val isSelected = uiState.selectedProvider == provider
                    val isConfigured = provider in uiState.configuredProviders
                    ProviderCard(
                        provider = provider,
                        isSelected = isSelected,
                        isConfigured = isConfigured,
                        onClick = { onSelectProvider(provider) },
                        onSignup = { uriHandler.openUri(provider.signupUrl) }
                    )
                }

                // API Key for selected provider
                item {
                    SectionHeader("${uiState.selectedProvider.displayName} API Key")
                    FitTrackCard {
                        var showKey by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = uiState.apiKey,
                            onValueChange = onApiKeyChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(uiState.selectedProvider.keyHint, color = TextTertiary) },
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                            Spacer(Modifier.weight(1f))
                            TextButton(
                                onClick = { uriHandler.openUri(uiState.selectedProvider.signupUrl) }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.OpenInNew,
                                    "Get key",
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Get API Key", color = ElectricBlue)
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
private fun ProviderCard(
    provider: LlmProvider,
    isSelected: Boolean,
    isConfigured: Boolean,
    onClick: () -> Unit,
    onSignup: () -> Unit
) {
    val borderColor = when {
        isSelected -> ElectricBlue
        isConfigured -> SuccessGreen.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    FitTrackCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection indicator
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) ElectricBlue else DarkSurfaceVariant)
                    .then(
                        if (!isSelected) Modifier.border(1.dp, TextTertiary, CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        provider.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (isConfigured) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SuccessGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "KEY SET",
                                style = MaterialTheme.typography.labelSmall,
                                color = SuccessGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    provider.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    provider.pricingNote,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!isConfigured) {
                TextButton(onClick = onSignup) {
                    Text("Sign Up", color = ElectricBlue, style = MaterialTheme.typography.labelMedium)
                }
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
