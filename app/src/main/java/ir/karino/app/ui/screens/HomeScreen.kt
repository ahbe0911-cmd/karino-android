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
import ir.karino.app.ui.components.CompactCategoryMenu
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
            start = 16.dp,
            end = 16.dp,
            top = 14.dp,
            bottom = 104.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Column(Modifier.padding(horizontal = 10.dp)) {
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
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.FormatQuote,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column(Modifier.padding(horizontal = 8.dp)) {
                            Text(
                                state.dailyQuote.text,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3,
                            )
                            Text(
                                "— ${state.dailyQuote.author}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "فهرست امروز",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        "${state.tasks.size} کار برای امروز".toPersianDigits(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                CompactCategoryMenu(
                    categories = state.categories,
                    selectedCategoryId = state.selectedCategoryId,
                    onSelected = onCategorySelected,
                )
            }
        }

        if (state.tasks.isEmpty()) {
            item {
                EmptyTasks(
                    title = "امروز هنوز کاری نداری",
                    subtitle = "با یک قدم کوچک شروع کن؛ دکمهٔ + همین نزدیکی است.",
                )
            }
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
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(Navy, TealPrimary),
                    ),
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${weekdays.getValue(today.dayOfWeek)}، ${JalaliDate.today().format()}",
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    when {
                        total == 0 -> "روز خلوتی داری"
                        done == total -> "همه‌چیز انجام شد!"
                        else -> "${total - done} کار باقی مانده".toPersianDigits()
                    },
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(52.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = Color.White.copy(alpha = 0.18f),
                    strokeWidth = 5.dp,
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
