package ir.karino.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.karino.app.data.local.TaskEntity
import ir.karino.app.ui.KarinoUiState
import ir.karino.app.ui.components.CompactCategoryMenu
import ir.karino.app.ui.components.EmptyTasks
import ir.karino.app.ui.components.TaskCard
import ir.karino.app.util.toPersianDigits

@Composable
fun HomeScreen(
    state: KarinoUiState,
    onCategorySelected: (Long?) -> Unit,
    onCheckTask: (TaskEntity, Boolean) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoriesById = remember(state.categories) {
        state.categories.associateBy { it.id }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 104.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("کارینو", style = MaterialTheme.typography.titleLarge)
                Text(
                    "امروز؛ ساده و انجام‌شدنی",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (state.settings.showDailyQuote) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        Icons.Outlined.FormatQuote,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
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
                    category = task.categoryId?.let { categoriesById[it] },
                    onCheckedChange = { onCheckTask(task, it) },
                    onEdit = { onEditTask(task) },
                    onDelete = { onDeleteTask(task) },
                )
            }
        }
    }
}
