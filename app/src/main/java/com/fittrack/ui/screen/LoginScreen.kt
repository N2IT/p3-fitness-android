package com.fittrack.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittrack.ui.components.*
import com.fittrack.ui.theme.*
import com.fittrack.ui.viewmodel.LoginUiState

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onUsernameChanged: (String) -> Unit,
    onUnitToggle: () -> Unit,
    onLogin: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    // Animate content in
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "contentAlpha"
    )

    // Glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Logo with electric blue glow
            Box(contentAlignment = Alignment.Center) {
                // Glow effect
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    ElectricBlue.copy(alpha = glowAlpha),
                                    Color.Transparent
                                ),
                                radius = 200f
                            )
                        )
                )
                Text(
                    text = "FitTrack",
                    style = TextStyle(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp,
                        shadow = Shadow(
                            color = ElectricBlue.copy(alpha = 0.5f),
                            offset = Offset(0f, 0f),
                            blurRadius = 20f
                        )
                    ),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Track Every Rep",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading / Ready state
            if (!uiState.isDbReady) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Preparing your gym...",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            } else {
                // Username field
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = onUsernameChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Enter username", color = TextTertiary)
                    },
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
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        onLogin()
                    })
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Unit selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weight Unit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    UnitToggle(
                        selectedUnit = uiState.unitPreference,
                        onToggle = onUnitToggle
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Error message
                if (uiState.error != null) {
                    Text(
                        text = uiState.error,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Login button
                ElectricButton(
                    text = if (uiState.isLoading) "Loading..." else "Get Started",
                    onClick = {
                        focusManager.clearFocus()
                        onLogin()
                    },
                    enabled = !uiState.isLoading && uiState.username.isNotBlank()
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
