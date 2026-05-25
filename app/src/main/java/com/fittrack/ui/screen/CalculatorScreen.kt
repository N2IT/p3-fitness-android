package com.fittrack.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fittrack.ui.components.*
import com.fittrack.ui.theme.*

enum class CalcTab { ONE_RM, PLATE }

@Composable
fun CalculatorScreen(onBack: () -> Unit) {
    var activeTab by remember { mutableStateOf(CalcTab.ONE_RM) }

    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Calculator", fontWeight = FontWeight.Bold) },
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
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Tab toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .padding(4.dp)
                ) {
                    listOf(CalcTab.ONE_RM to "1RM Calculator", CalcTab.PLATE to "Plate Calculator").forEach { (tab, label) ->
                        val selected = activeTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) ElectricBlue else Color.Transparent)
                                .clickable { activeTab = tab }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = if (selected) Color.White else TextSecondary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                when (activeTab) {
                    CalcTab.ONE_RM -> OneRmCalculator()
                    CalcTab.PLATE -> PlateCalculator()
                }
            }
        }
    }
}

@Composable
private fun OneRmCalculator() {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }

    val oneRm = remember(weight, reps) {
        val w = weight.toDoubleOrNull() ?: return@remember null
        val r = reps.toIntOrNull() ?: return@remember null
        if (w <= 0 || r <= 0) return@remember null
        if (r == 1) w else w * (1 + r / 30.0)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            FitTrackCard {
                Text("Enter your lift", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                        modifier = Modifier.weight(1f),
                        label = { Text("Weight", color = TextTertiary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue, unfocusedBorderColor = DarkSurfaceVariant,
                            cursorColor = ElectricBlue, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurface, unfocusedContainerColor = DarkSurface
                        )
                    )
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.weight(1f),
                        label = { Text("Reps", color = TextTertiary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue, unfocusedBorderColor = DarkSurfaceVariant,
                            cursorColor = ElectricBlue, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurface, unfocusedContainerColor = DarkSurface
                        )
                    )
                }
            }
        }

        if (oneRm != null) {
            item {
                FitTrackCard {
                    Text("Estimated 1RM", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                    Text(
                        String.format("%.1f", oneRm),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = ElectricBlue
                    )
                }
            }

            // Percentage table
            item {
                FitTrackCard {
                    Text("Percentage Table", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Text("% 1RM", style = MaterialTheme.typography.labelSmall, color = TextTertiary, modifier = Modifier.weight(1f))
                        Text("Weight", style = MaterialTheme.typography.labelSmall, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("~Reps", style = MaterialTheme.typography.labelSmall, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                    Spacer(Modifier.height(4.dp))
                    listOf(100 to 1, 95 to 2, 90 to 3, 85 to 5, 80 to 6, 75 to 8, 70 to 10, 65 to 12, 60 to 15).forEach { (pct, approxReps) ->
                        val pctWeight = oneRm!! * pct / 100.0
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text("$pct%", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.weight(1f))
                            Text(String.format("%.1f", pctWeight), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("$approxReps", style = MaterialTheme.typography.bodyMedium, color = TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlateCalculator() {
    var targetWeight by remember { mutableStateOf("") }
    var barWeight by remember { mutableStateOf("45") }

    val plates = remember(targetWeight, barWeight) {
        val target = targetWeight.toDoubleOrNull() ?: return@remember null
        val bar = barWeight.toDoubleOrNull() ?: return@remember null
        if (target <= bar) return@remember null
        calculatePlates(target, bar)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            FitTrackCard {
                Text("Target Weight", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = targetWeight,
                        onValueChange = { targetWeight = it.filter { c -> c.isDigit() || c == '.' } },
                        modifier = Modifier.weight(1f),
                        label = { Text("Target", color = TextTertiary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue, unfocusedBorderColor = DarkSurfaceVariant,
                            cursorColor = ElectricBlue, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurface, unfocusedContainerColor = DarkSurface
                        )
                    )
                    OutlinedTextField(
                        value = barWeight,
                        onValueChange = { barWeight = it.filter { c -> c.isDigit() || c == '.' } },
                        modifier = Modifier.weight(1f),
                        label = { Text("Bar Weight", color = TextTertiary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue, unfocusedBorderColor = DarkSurfaceVariant,
                            cursorColor = ElectricBlue, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurface, unfocusedContainerColor = DarkSurface
                        )
                    )
                }
            }
        }

        if (plates != null) {
            item {
                FitTrackCard {
                    Text("Per Side", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(12.dp))

                    // Visual barbell
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        plates.forEach { (plate, count) ->
                            repeat(count) {
                                val color = when {
                                    plate >= 45 -> ElectricBlue
                                    plate >= 25 -> AccentOrange
                                    plate >= 10 -> SuccessGreen
                                    else -> TextSecondary
                                }
                                val height = (30 + plate * 0.8).coerceAtMost(80.0).dp
                                Box(
                                    modifier = Modifier
                                        .width(12.dp)
                                        .height(height)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(color)
                                )
                                Spacer(Modifier.width(2.dp))
                            }
                        }
                        // Bar
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(8.dp)
                                .background(TextTertiary, RoundedCornerShape(4.dp))
                        )
                        plates.reversed().forEach { (plate, count) ->
                            repeat(count) {
                                Spacer(Modifier.width(2.dp))
                                val color = when {
                                    plate >= 45 -> ElectricBlue
                                    plate >= 25 -> AccentOrange
                                    plate >= 10 -> SuccessGreen
                                    else -> TextSecondary
                                }
                                val height = (30 + plate * 0.8).coerceAtMost(80.0).dp
                                Box(
                                    modifier = Modifier
                                        .width(12.dp)
                                        .height(height)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(color)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Plate list
                    plates.forEach { (plate, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${plate}lb plate", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                            Text("× $count", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = ElectricBlue)
                        }
                    }
                }
            }
        }
    }
}

private fun calculatePlates(target: Double, bar: Double): List<Pair<Double, Int>>? {
    var remaining = (target - bar) / 2.0
    if (remaining < 0) return null

    val availablePlates = listOf(45.0, 35.0, 25.0, 10.0, 5.0, 2.5)
    val result = mutableListOf<Pair<Double, Int>>()

    for (plate in availablePlates) {
        val count = (remaining / plate).toInt()
        if (count > 0) {
            result.add(plate to count)
            remaining -= plate * count
        }
    }

    return result
}
