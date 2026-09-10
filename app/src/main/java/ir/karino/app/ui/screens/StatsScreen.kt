package ir.karino.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.karino.app.ui.KarinoUiState
import ir.karino.app.util.toPersianDigits
import kotlin.math.roundToInt

@Composable
fun StatsScreen(
    state: KarinoUiState,
    modifier: Modifier = Modifier,
) {
    val total = state.activeCount + state.completedCount
    val completionRate = if (total == 0) 0f else state.completedCount.toFloat() / total
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp)
            .padding(bottom = 100.dp),
    ) {
        Text("گزارش پیشرفت", style = MaterialTheme.typography.headlineSmall)
        Text(
            "یک نگاه ساده به روند انجام کارها",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatCard(
                title = "باز",
                value = state.activeCount,
                iconColor = MaterialTheme.colorScheme.primary,
                icon = { Icon(Icons.Outlined.RadioButtonUnchecked, contentDescription = null) },
                modifier = Modifier.weight(1f),
            )
            StatCard(
                title = "انجام‌شده",
                value = state.completedCount,
                iconColor = Color(0xFF2F9B67),
                icon = { Icon(Icons.Outlined.CheckCircleOutline, contentDescription = null) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.AutoGraph,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        "نرخ تکمیل کل",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .weight(1f),
                    )
                    Text(
                        "${(completionRate * 100).roundToInt()}٪".toPersianDigits(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { completionRate },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("هفت روز گذشته", style = MaterialTheme.typography.titleMedium)
                Text(
                    "تعداد کارهایی که هر روز تمام کرده‌اید",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(20.dp))
                WeeklyBars(state)
            }
        }
    }
}

@Composable
private fun WeeklyBars(state: KarinoUiState) {
    val max = (state.weeklyCompletion.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        state.weeklyCompletion.forEach { day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    day.count.toString().toPersianDigits(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .heightIn(min = 8.dp)
                        .height((8 + (day.count.toFloat() / max * 92)).dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                        ),
                )
                Spacer(Modifier.height(7.dp))
                Text(day.label, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: Int,
    iconColor: Color,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(iconColor.copy(alpha = 0.14f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides iconColor,
                ) { icon() }
            }
            Spacer(Modifier.height(14.dp))
            Text(value.toString().toPersianDigits(), style = MaterialTheme.typography.headlineMedium)
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
