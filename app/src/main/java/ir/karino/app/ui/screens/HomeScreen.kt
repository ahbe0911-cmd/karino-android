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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.karino.app.data.local.TaskEntity
import ir.karino.app.ui.KarinoUiState
import ir.karino.app.ui.components.CategorySelector
import ir.karino.app.ui.components.EmptyTasks
import ir.karino.app.ui.components.TaskCard
import ir.karino.app.ui.theme.Navy
import ir.karino.app.ui.theme.TealPrimary
import ir.karino.app.util.JalaliDate
import ir.karino.app.util.toPersianDigits
import java.time.LocalDate

@Composable
fun HomeScreen(
    state: KarinoUiState,
    onCategorySelected: (Long?) -> Unit,
    onCheckTask: (TaskEntity, Boolean) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 18.dp,
            bottom = 104.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Column(Modifier.padding(horizontal = 12.dp)) {
                    Text("کارینو", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "کارهای امروز، ساده و روشن",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            TodayHero(
                total = state.todayTotal,
                done = state.todayDone,
            )
        }

        if (state.settings.showDailyQuote) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            Icons.Outlined.FormatQuote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column(Modifier.padding(horizontal = 10.dp)) {
                            Text(
                                state.dailyQuote.text,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                state.dailyQuote.author,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        item {
            CategorySelector(
                categories = state.categories,
                selectedCategoryId = state.selectedCategoryId,
                onSelected = onCategorySelected,
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "فهرست امروز",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${state.tasks.size} کار".toPersianDigits(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (state.tasks.isEmpty()) {
            item { EmptyTasks() }
        } else {
            items(state.tasks, key = TaskEntity::id) { task ->
                TaskCard(
                    task = task,
                    category = state.categories.firstOrNull { it.id == task.categoryId },
                    onCheckedChange = { onCheckTask(task, it) },
                    onEdit = { onEditTask(task) },
                    onDelete = { onDeleteTask(task) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun TodayHero(total: Int, done: Int) {
    val today = LocalDate.now()
    val weekdays = mapOf(
        java.time.DayOfWeek.SATURDAY to "شنبه",
        java.time.DayOfWeek.SUNDAY to "یکشنبه",
        java.time.DayOfWeek.MONDAY to "دوشنبه",
        java.time.DayOfWeek.TUESDAY to "سه‌شنبه",
        java.time.DayOfWeek.WEDNESDAY to "چهارشنبه",
        java.time.DayOfWeek.THURSDAY to "پنجشنبه",
        java.time.DayOfWeek.FRIDAY to "جمعه",
    )
    val progress = if (total == 0) 0f else done.toFloat() / total
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(Navy, TealPrimary),
                    ),
                )
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${weekdays.getValue(today.dayOfWeek)}، ${JalaliDate.today().format()}",
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    when {
                        total == 0 -> "روز خلوتی داری"
                        done == total -> "همه‌چیز انجام شد!"
                        else -> "${total - done} کار باقی مانده".toPersianDigits()
                    },
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(66.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = Color.White.copy(alpha = 0.18f),
                    strokeWidth = 7.dp,
                )
                Text(
                    "$done/$total".toPersianDigits(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
