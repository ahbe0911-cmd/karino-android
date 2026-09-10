package ir.karino.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ir.karino.app.data.local.CategoryEntity
import ir.karino.app.data.local.TaskEntity
import ir.karino.app.domain.model.RepeatRule
import ir.karino.app.domain.model.TaskDraft
import ir.karino.app.domain.model.TaskPriority
import ir.karino.app.util.JalaliDate
import ir.karino.app.util.formatJalaliDateTime
import ir.karino.app.util.toPersianDigits
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorSheet(
    task: TaskEntity?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (TaskDraft) -> Unit,
    onReminderPermissionNeeded: () -> Unit,
) {
    var title by remember(task?.id) { mutableStateOf(task?.title.orEmpty()) }
    var note by remember(task?.id) { mutableStateOf(task?.note.orEmpty()) }
    var categoryId by remember(task?.id) { mutableStateOf(task?.categoryId) }
    var priority by remember(task?.id) { mutableStateOf(task?.priority ?: TaskPriority.NORMAL) }
    var dueAt by remember(task?.id) { mutableStateOf(task?.dueAt) }
    var reminderEnabled by remember(task?.id) { mutableStateOf(task?.reminderAt != null) }
    var reminderOffsetMinutes by remember(task?.id) {
        mutableStateOf(
            task?.let { current ->
                val due = current.dueAt
                val reminder = current.reminderAt
                if (due != null && reminder != null) {
                    ((due - reminder) / 60_000L).toInt().coerceAtLeast(0)
                } else {
                    0
                }
            } ?: 0,
        )
    }
    var repeatRule by remember(task?.id) { mutableStateOf(task?.repeatRule ?: RepeatRule.NONE) }
    var showDateDialog by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }
    var scheduleError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (task == null) "کار تازه" else "ویرایش کار",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "بستن")
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it.take(180)
                    if (it.isNotBlank()) titleError = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("عنوان کار") },
                placeholder = { Text("مثلاً تماس با کتابخانه") },
                singleLine = true,
                isError = titleError,
                supportingText = if (titleError) {
                    { Text("عنوان کار را بنویسید.") }
                } else null,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(2_000) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("یادداشت (اختیاری)") },
                minLines = 2,
                maxLines = 4,
            )

            SectionTitle("دسته‌بندی")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = categoryId == null,
                    onClick = { categoryId = null },
                    label = { Text("بدون دسته") },
                )
                categories.forEach { category ->
                    FilterChip(
                        selected = categoryId == category.id,
                        onClick = { categoryId = category.id },
                        label = { Text(category.name) },
                    )
                }
            }

            SectionTitle("اهمیت")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TaskPriority.entries.forEach { item ->
                    FilterChip(
                        selected = priority == item,
                        onClick = { priority = item },
                        label = { Text(item.title) },
                    )
                }
            }

            SectionTitle("زمان‌بندی")
            if (dueAt == null) {
                OutlinedButton(
                    onClick = { showDateDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                    Text("  افزودن تاریخ و ساعت شمسی")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = { showDateDialog = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                        Text("  ${dueAt!!.formatJalaliDateTime()}")
                    }
                    IconButton(
                        onClick = {
                            dueAt = null
                            reminderEnabled = false
                            repeatRule = RepeatRule.NONE
                            scheduleError = null
                        },
                    ) {
                        Icon(Icons.Outlined.Close, contentDescription = "حذف زمان")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        "یادآوری در زمان کار",
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .weight(1f),
                    )
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = {
                            reminderEnabled = it
                            scheduleError = null
                        },
                    )
                }
                if (reminderEnabled) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        REMINDER_OFFSETS.forEach { option ->
                            FilterChip(
                                selected = reminderOffsetMinutes == option.minutes,
                                onClick = {
                                    reminderOffsetMinutes = option.minutes
                                    scheduleError = null
                                },
                                label = { Text(option.title) },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Repeat, contentDescription = null)
                    RepeatRule.entries.forEach { item ->
                        FilterChip(
                            selected = repeatRule == item,
                            onClick = { repeatRule = item },
                            label = { Text(item.title) },
                        )
                    }
                }
            }

            scheduleError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    val reminderAt = if (reminderEnabled && dueAt != null) {
                        dueAt!! - reminderOffsetMinutes * 60_000L
                    } else {
                        null
                    }
                    if (reminderAt != null && reminderAt <= System.currentTimeMillis()) {
                        scheduleError = "زمان یادآوری باید در آینده باشد."
                        return@Button
                    }
                    if (reminderEnabled) onReminderPermissionNeeded()
                    onSave(
                        TaskDraft(
                            id = task?.id ?: 0,
                            title = title,
                            note = note,
                            categoryId = categoryId,
                            priority = priority,
                            dueAt = dueAt,
                            reminderAt = reminderAt,
                            repeatRule = repeatRule,
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (task == null) "ذخیرهٔ کار" else "ذخیرهٔ تغییرات")
            }
        }
    }

    if (showDateDialog) {
        PersianDateTimeDialog(
            initialEpochMillis = dueAt,
            onDismiss = { showDateDialog = false },
            onConfirm = {
                dueAt = it
                scheduleError = null
                showDateDialog = false
            },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp),
    )
}

@Composable
fun PersianDateTimeDialog(
    initialEpochMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    val zone = remember { ZoneId.systemDefault() }
    val initial = remember(initialEpochMillis) {
        Instant.ofEpochMilli(initialEpochMillis ?: System.currentTimeMillis() + 60 * 60 * 1_000L)
            .atZone(zone)
    }
    val initialJalali = remember(initial) {
        JalaliDate.fromGregorian(initial.year, initial.monthValue, initial.dayOfMonth)
    }
    var year by remember { mutableStateOf(initialJalali.year.toString().toPersianDigits()) }
    var month by remember { mutableStateOf(initialJalali.month.toString().toPersianDigits()) }
    var day by remember { mutableStateOf(initialJalali.day.toString().toPersianDigits()) }
    var hour by remember { mutableStateOf(initial.hour.toString().padStart(2, '0').toPersianDigits()) }
    var minute by remember { mutableStateOf(initial.minute.toString().padStart(2, '0').toPersianDigits()) }

    val parsedYear = year.toEnglishDigits().toIntOrNull()
    val parsedMonth = month.toEnglishDigits().toIntOrNull()
    val parsedDay = day.toEnglishDigits().toIntOrNull()
    val parsedHour = hour.toEnglishDigits().toIntOrNull()
    val parsedMinute = minute.toEnglishDigits().toIntOrNull()
    val parsedDate = if (parsedYear != null && parsedMonth != null && parsedDay != null) {
        JalaliDate(parsedYear, parsedMonth, parsedDay)
    } else null
    val isValid = parsedDate?.isValid() == true &&
        parsedHour != null && parsedHour in 0..23 &&
        parsedMinute != null && parsedMinute in 0..59

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تاریخ و ساعت") },
        text = {
            Column {
                Text(
                    "تاریخ را به‌صورت شمسی و ساعت را ۲۴ ساعته وارد کنید.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(
                        value = day,
                        onValueChange = { day = it.take(2) },
                        label = "روز",
                        modifier = Modifier.weight(0.8f),
                    )
                    NumberField(
                        value = month,
                        onValueChange = { month = it.take(2) },
                        label = "ماه",
                        modifier = Modifier.weight(0.8f),
                    )
                    NumberField(
                        value = year,
                        onValueChange = { year = it.take(4) },
                        label = "سال",
                        modifier = Modifier.weight(1.2f),
                    )
                }
                if (parsedMonth != null && parsedMonth in 1..12) {
                    Text(
                        JalaliDate.MONTH_NAMES[parsedMonth!! - 1],
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NumberField(
                        value = minute,
                        onValueChange = { minute = it.take(2) },
                        label = "دقیقه",
                        modifier = Modifier.weight(1f),
                    )
                    Text(":", style = MaterialTheme.typography.titleLarge)
                    NumberField(
                        value = hour,
                        onValueChange = { hour = it.take(2) },
                        label = "ساعت",
                        modifier = Modifier.weight(1f),
                    )
                }
                if (!isValid) {
                    Text(
                        "تاریخ یا ساعت واردشده معتبر نیست.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                TextButton(
                    onClick = {
                        val today = JalaliDate.today(zone)
                        year = today.year.toString().toPersianDigits()
                        month = today.month.toString().toPersianDigits()
                        day = today.day.toString().toPersianDigits()
                    },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("امروز")
                }
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    val epoch = parsedDate!!.toEpochMillis(parsedHour!!, parsedMinute!!, zone)
                    onConfirm(epoch)
                },
            ) {
                Text("تأیید")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        },
    )
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.all { it.isDigit() || it in '۰'..'۹' }) onValueChange(newValue)
        },
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

private fun String.toEnglishDigits(): String = buildString(length) {
    this@toEnglishDigits.forEach { character ->
        append(
            when (character) {
                '۰' -> '0'
                '۱' -> '1'
                '۲' -> '2'
                '۳' -> '3'
                '۴' -> '4'
                '۵' -> '5'
                '۶' -> '6'
                '۷' -> '7'
                '۸' -> '8'
                '۹' -> '9'
                else -> character
            },
        )
    }
}

private data class ReminderOffset(val title: String, val minutes: Int)

private val REMINDER_OFFSETS = listOf(
    ReminderOffset("همان زمان", 0),
    ReminderOffset("۱۰ دقیقه قبل", 10),
    ReminderOffset("۳۰ دقیقه قبل", 30),
    ReminderOffset("۱ ساعت قبل", 60),
    ReminderOffset("۱ روز قبل", 24 * 60),
)
