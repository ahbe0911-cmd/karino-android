package ir.karino.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.karino.app.data.local.TaskEntity
import ir.karino.app.ui.KarinoUiState
import ir.karino.app.ui.components.EmptyTasks
import ir.karino.app.ui.components.TaskCard

@Composable
fun HomeScreen(
    state: KarinoUiState,
    onCheckTask: (TaskEntity, Boolean) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 104.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.settings.showDailyQuote) {
            item(key = "daily-note") {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = state.dailyQuote.text,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        if (state.tasks.isEmpty()) {
            item(key = "empty") {
                EmptyTasks(
                    title = "اینجا خلوت است",
                    subtitle = "با + یک یادداشت کوتاه بساز.",
                )
            }
        } else {
            items(
                items = state.tasks,
                key = TaskEntity::id,
                contentType = { "task" },
            ) { task ->
                TaskCard(
                    task = task,
                    onCheckedChange = { onCheckTask(task, it) },
                    onEdit = { onEditTask(task) },
                )
            }
        }
    }
}
