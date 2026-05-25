package com.fittrack.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittrack.ui.theme.*

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        DarkSurface,
                        DarkBackground
                    )
                )
            ),
        content = content
    )
}

@Composable
fun FitTrackCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun ElectricButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isOrange: Boolean = false
) {
    val color = if (isOrange) AccentOrange else ElectricBlue
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun SweepProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = 4.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    androidx.compose.foundation.Canvas(
        modifier = modifier.size(size)
    ) {
        val sweep = animatedProgress * 360f
        drawArc(
            color = DarkSurfaceVariant,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
        if (sweep > 0) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(ElectricBlue, AccentOrange, ElectricBlue)
                ),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val offset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        DarkSurfaceVariant.copy(alpha = 0.3f),
                        DarkSurfaceVariant.copy(alpha = 0.6f),
                        DarkSurfaceVariant.copy(alpha = 0.3f)
                    ),
                    start = Offset(offset, 0f),
                    end = Offset(offset + 300f, 0f)
                )
            )
    )
}

@Composable
fun UnitToggle(
    selectedUnit: String,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        listOf("lbs", "kg").forEach { unit ->
            val isSelected = unit == selectedUnit
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) ElectricBlue else Color.Transparent,
                label = "unitBg"
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .clickable { onToggle() }
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unit,
                    color = if (isSelected) Color.White else TextSecondary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun EquipmentBadge(
    equipment: String,
    modifier: Modifier = Modifier
) {
    val color = when (equipment) {
        "Barbell" -> ElectricBlue
        "Dumbbell" -> AccentOrange
        "Cable" -> SuccessGreen
        "Machine" -> WarningYellow
        "Bodyweight" -> TextSecondary
        else -> TextTertiary
    }
    Text(
        text = equipment,
        modifier = modifier
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        color = color,
        style = MaterialTheme.typography.labelSmall
    )
}

@Composable
fun MuscleGroupChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) ElectricBlue else DarkSurfaceVariant,
        label = "chipBg"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = name,
            color = if (isSelected) Color.White else TextSecondary,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun EmptyStateMessage(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%d:%02d", minutes, secs)
    }
}
