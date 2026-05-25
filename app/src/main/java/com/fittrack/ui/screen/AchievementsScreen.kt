package com.fittrack.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fittrack.ui.components.*
import com.fittrack.ui.theme.*

data class Achievement(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val category: String,
    val threshold: Int,
    val currentProgress: Int,
    val isUnlocked: Boolean
)

@Composable
fun AchievementsScreen(
    totalWorkouts: Int,
    onBack: () -> Unit
) {
    val achievements = remember(totalWorkouts) {
        listOf(
            // Consistency
            Achievement("First Step", "Complete your first workout", Icons.Default.DirectionsRun, "Consistency", 1, totalWorkouts, totalWorkouts >= 1),
            Achievement("Regular", "Complete 10 workouts", Icons.Default.Repeat, "Consistency", 10, totalWorkouts, totalWorkouts >= 10),
            Achievement("Dedicated", "Complete 25 workouts", Icons.Default.Whatshot, "Consistency", 25, totalWorkouts, totalWorkouts >= 25),
            Achievement("Iron Will", "Complete 50 workouts", Icons.Default.Shield, "Consistency", 50, totalWorkouts, totalWorkouts >= 50),
            Achievement("Legendary", "Complete 100 workouts", Icons.Default.EmojiEvents, "Consistency", 100, totalWorkouts, totalWorkouts >= 100),
            // Strength (placeholder - would need PR data)
            Achievement("Getting Strong", "Set your first PR", Icons.Default.TrendingUp, "Strength", 1, 0, false),
            Achievement("PR Machine", "Set 10 PRs", Icons.Default.Bolt, "Strength", 10, 0, false),
            Achievement("Beast Mode", "Set 25 PRs", Icons.Default.Star, "Strength", 25, 0, false),
            // Explorer
            Achievement("Explorer", "Try 10 different exercises", Icons.Default.Explore, "Explorer", 10, 0, false),
            Achievement("Well Rounded", "Train all muscle groups", Icons.Default.FitnessCenter, "Explorer", 6, 0, false),
        )
    }

    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Achievements", fontWeight = FontWeight.Bold) },
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
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                // Streak counter
                FitTrackCard(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Whatshot, "Streak", tint = AccentOrange, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "$totalWorkouts Total Workouts",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                // Badge grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(achievements) { achievement ->
                        AchievementCard(achievement)
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    val cardColor = if (achievement.isUnlocked) DarkCard else DarkCard.copy(alpha = 0.5f)
    val iconTint = if (achievement.isUnlocked) AccentOrange else TextTertiary.copy(alpha = 0.4f)
    val textColor = if (achievement.isUnlocked) TextPrimary else TextTertiary.copy(alpha = 0.5f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (achievement.isUnlocked) {
                            Brush.linearGradient(listOf(AccentOrange, ElectricBlue))
                        } else {
                            Brush.linearGradient(listOf(TextTertiary.copy(alpha = 0.2f), TextTertiary.copy(alpha = 0.1f)))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(achievement.icon, achievement.name, tint = if (achievement.isUnlocked) Color.White else TextTertiary, modifier = Modifier.size(24.dp))
            }

            Spacer(Modifier.height(8.dp))

            Text(
                achievement.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
            Text(
                achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (achievement.isUnlocked) TextSecondary else TextTertiary.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            if (!achievement.isUnlocked && achievement.threshold > 0) {
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (achievement.currentProgress.toFloat() / achievement.threshold).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = ElectricBlue.copy(alpha = 0.5f),
                    trackColor = DarkSurfaceVariant
                )
                Text(
                    "${achievement.currentProgress}/${achievement.threshold}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }
    }
}
