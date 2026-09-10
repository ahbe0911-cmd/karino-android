package ir.karino.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.karino.app.data.local.TaskEntity
import ir.karino.app.domain.model.TaskFilter
import ir.karino.app.ui.KarinoUiState
import ir.karino.app.ui.components.CategorySelector
import ir.karino.app.ui.components.EmptyTasks
import ir.karino.app.ui.components.TaskCard
import ir.karino.app.util.toPersianDigits

@Composable
fun TasksScreen(
    state: KarinoUiState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (TaskFilter) -> Unit,
    onCategorySelected: (Long?) -> Unit,
    onCheckTask: (TaskEntity, Boolean) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoriesById = remember(state.categories) {
        state.categories.associateBy { it.id }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 18.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("همهٔ کارها", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "${state.activeCount} کار باز".toPersianDigits(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            placeholder = { Text("جستجو در عنوان و یادداشت") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = if (state.query.isNotEmpty()) {
                {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Outlined.Clear, contentDescription = "پاک کردن جستجو")
                    }
                }
            } else null,
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge,
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(TaskFilter.entries, key = TaskFilter::name) { filter ->
                FilterChip(
                    selected = state.filter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(filter.title) },
                )
            }
        }

        CategorySelector(
            categories = state.categories,
            selectedCategoryId = state.selectedCategoryId,
            onSelected = onCategorySelected,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
        )

        if (state.tasks.isEmpty()) {
            EmptyTasks(
                title = if (state.query.isBlank()) "کاری در این بخش نیست" else "نتیجه‌ای پیدا نشد",
                subtitle = if (state.query.isBlank()) {
                    "فیلتر دیگری را انتخاب کنید یا یک کار بسازید."
                } else {
                    "عبارت جستجو یا فیلترها را تغییر دهید."
                },
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 8.dp,
                    bottom = 104.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
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
}
