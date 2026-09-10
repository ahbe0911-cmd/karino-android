package ir.karino.app.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.karino.app.data.backup.BackupManager
import ir.karino.app.data.local.CategoryEntity
import ir.karino.app.data.local.TaskEntity
import ir.karino.app.data.preferences.SettingsRepository
import ir.karino.app.data.repository.TaskRepository
import ir.karino.app.domain.model.AppSettings
import ir.karino.app.domain.model.TaskDraft
import ir.karino.app.domain.model.TaskFilter
import ir.karino.app.domain.model.ThemeMode
import ir.karino.app.util.isSameLocalDay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DailyQuote(val text: String, val author: String)

data class DayCompletion(
    val label: String,
    val count: Int,
)

data class KarinoUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val allTasks: List<TaskEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val query: String = "",
    val filter: TaskFilter = TaskFilter.TODAY,
    val selectedCategoryId: Long? = null,
    val todayTotal: Int = 0,
    val todayDone: Int = 0,
    val activeCount: Int = 0,
    val completedCount: Int = 0,
    val weeklyCompletion: List<DayCompletion> = emptyList(),
    val dailyQuote: DailyQuote = DailyQuote("هر کار بزرگ، از یک قدم کوچک شروع می‌شود.", "کارینو"),
)

private data class SourceData(
    val tasks: List<TaskEntity>,
    val categories: List<CategoryEntity>,
    val settings: AppSettings,
)

private data class Controls(
    val query: String,
    val filter: TaskFilter,
    val selectedCategoryId: Long?,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(TaskFilter.TODAY)
    private val selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages = _messages.asSharedFlow()

    private val source = combine(
        taskRepository.tasks,
        taskRepository.categories,
        settingsRepository.settings,
    ) { tasks, categories, settings ->
        SourceData(tasks, categories, settings)
    }

    private val controls = combine(query, filter, selectedCategoryId) { query, filter, categoryId ->
        Controls(query.trim(), filter, categoryId)
    }

    val uiState = combine(source, controls) { source, controls ->
        buildUiState(source, controls)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = KarinoUiState(),
    )

    init {
        viewModelScope.launch { taskRepository.ensureDefaultCategories() }
    }

    fun setQuery(value: String) {
        query.value = value
    }

    fun setFilter(value: TaskFilter) {
        filter.value = value
    }

    fun selectCategory(categoryId: Long?) {
        selectedCategoryId.value = categoryId
    }

    fun saveTask(draft: TaskDraft) {
        viewModelScope.launch {
            runCatching { taskRepository.save(draft) }
                .onSuccess { _messages.emit(if (draft.id == 0L) "کار جدید ذخیره شد." else "تغییرات ذخیره شد.") }
                .onFailure { _messages.emit(it.message ?: "ذخیرهٔ کار انجام نشد.") }
        }
    }

    fun setCompleted(task: TaskEntity, completed: Boolean) {
        viewModelScope.launch {
            runCatching { taskRepository.setCompleted(task.id, completed) }
                .onFailure { _messages.emit("تغییر وضعیت کار انجام نشد.") }
        }
    }

    suspend fun deleteTask(task: TaskEntity): Boolean =
        runCatching { taskRepository.delete(task) }
            .onFailure { _messages.emit("حذف کار انجام نشد.") }
            .isSuccess

    suspend fun restoreTask(task: TaskEntity) {
        runCatching { taskRepository.restore(task) }
            .onFailure { _messages.emit("بازگردانی کار انجام نشد.") }
    }

    fun deleteCompleted() {
        viewModelScope.launch {
            runCatching { taskRepository.deleteCompleted() }
                .onSuccess { _messages.emit("کارهای انجام‌شده پاک شدند.") }
                .onFailure { _messages.emit("پاک‌سازی انجام نشد.") }
        }
    }

    fun addCategory(name: String, colorArgb: Int) {
        viewModelScope.launch {
            runCatching { taskRepository.addCategory(name, colorArgb) }
                .onSuccess { _messages.emit("دسته‌بندی ساخته شد.") }
                .onFailure { _messages.emit(it.message ?: "ساخت دسته‌بندی انجام نشد.") }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            runCatching { taskRepository.deleteCategory(category) }
                .onSuccess {
                    if (selectedCategoryId.value == category.id) selectedCategoryId.value = null
                    _messages.emit("دسته‌بندی حذف شد؛ کارهای آن باقی ماندند.")
                }
                .onFailure { _messages.emit("حذف دسته‌بندی انجام نشد.") }
        }
    }

    fun setThemeMode(value: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(value) }
    }

    fun setShowDailyQuote(value: Boolean) {
        viewModelScope.launch { settingsRepository.setShowDailyQuote(value) }
    }

    fun setShowCompletedTasks(value: Boolean) {
        viewModelScope.launch { settingsRepository.setShowCompletedTasks(value) }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            runCatching { backupManager.export(uri, taskRepository.snapshot()) }
                .onSuccess { _messages.emit("فایل پشتیبان ذخیره شد.") }
                .onFailure { _messages.emit(it.message ?: "ساخت فایل پشتیبان انجام نشد.") }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val snapshot = backupManager.import(uri)
                taskRepository.replaceAll(snapshot)
            }.onSuccess {
                selectedCategoryId.value = null
                query.value = ""
                _messages.emit("اطلاعات فایل پشتیبان بازیابی شد.")
            }.onFailure {
                _messages.emit(it.message ?: "بازیابی فایل انجام نشد.")
            }
        }
    }

    private fun buildUiState(source: SourceData, controls: Controls): KarinoUiState {
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val normalizedQuery = controls.query.lowercase()

        val visible = source.tasks.asSequence()
            .filter { task ->
                normalizedQuery.isBlank() ||
                    task.title.lowercase().contains(normalizedQuery) ||
                    task.note.lowercase().contains(normalizedQuery)
            }
            .filter { task ->
                controls.selectedCategoryId == null || task.categoryId == controls.selectedCategoryId
            }
            .filter { task ->
                when (controls.filter) {
                    TaskFilter.TODAY -> {
                        val dueDate = task.dueAt?.let {
                            Instant.ofEpochMilli(it).atZone(zone).toLocalDate()
                        }
                        val belongsToToday = if (task.isCompleted) {
                            task.completedAt?.isSameLocalDay(now, zone) == true || dueDate == today
                        } else {
                            dueDate == null || !dueDate.isAfter(today)
                        }
                        belongsToToday && (source.settings.showCompletedTasks || !task.isCompleted)
                    }
                    TaskFilter.UPCOMING -> {
                        !task.isCompleted && task.dueAt?.let {
                            Instant.ofEpochMilli(it).atZone(zone).toLocalDate().isAfter(today)
                        } == true
                    }
                    TaskFilter.ALL -> source.settings.showCompletedTasks || !task.isCompleted
                    TaskFilter.COMPLETED -> task.isCompleted
                }
            }
            .sortedWith(
                compareBy<TaskEntity> { it.isCompleted }
                    .thenByDescending { it.priority.ordinal }
                    .thenBy { it.dueAt ?: Long.MAX_VALUE }
                    .thenBy { it.sortOrder },
            )
            .toList()

        val todayTasks = source.tasks.filter { task ->
            val dueDate = task.dueAt?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
            if (task.isCompleted) {
                task.completedAt?.isSameLocalDay(now, zone) == true || dueDate == today
            } else {
                dueDate == null || !dueDate.isAfter(today)
            }
        }
        val weekDays = (6 downTo 0).map { offset -> today.minusDays(offset.toLong()) }
        val weekdayLabels = mapOf(
            java.time.DayOfWeek.SATURDAY to "ش",
            java.time.DayOfWeek.SUNDAY to "ی",
            java.time.DayOfWeek.MONDAY to "د",
            java.time.DayOfWeek.TUESDAY to "س",
            java.time.DayOfWeek.WEDNESDAY to "چ",
            java.time.DayOfWeek.THURSDAY to "پ",
            java.time.DayOfWeek.FRIDAY to "ج",
        )
        val weekly = weekDays.map { date ->
            DayCompletion(
                label = weekdayLabels.getValue(date.dayOfWeek),
                count = source.tasks.count { task ->
                    task.completedAt?.let {
                        Instant.ofEpochMilli(it).atZone(zone).toLocalDate() == date
                    } == true
                },
            )
        }

        return KarinoUiState(
            tasks = visible,
            allTasks = source.tasks,
            categories = source.categories,
            settings = source.settings,
            query = controls.query,
            filter = controls.filter,
            selectedCategoryId = controls.selectedCategoryId,
            todayTotal = todayTasks.size,
            todayDone = todayTasks.count(TaskEntity::isCompleted),
            activeCount = source.tasks.count { !it.isCompleted },
            completedCount = source.tasks.count(TaskEntity::isCompleted),
            weeklyCompletion = weekly,
            dailyQuote = QUOTES[today.dayOfYear % QUOTES.size],
        )
    }

    companion object {
        private val QUOTES = listOf(
            DailyQuote("انجام دادنِ کم، بهتر از برنامه‌ریزیِ بی‌پایان است.", "کارینو"),
            DailyQuote("روی کار بعدی تمرکز کن؛ نه روی تمام مسیر.", "کارینو"),
            DailyQuote("کارهای کوچکِ پیوسته، نتیجه‌های بزرگ می‌سازند.", "کارینو"),
            DailyQuote("امروز را ساده شروع کن؛ یک کار، یک تیک.", "کارینو"),
            DailyQuote("نظم یعنی تصمیم‌های خوب را آسان‌تر کنیم.", "کارینو"),
            DailyQuote("اول مهم‌ترین کار؛ باقی مسیر سبک‌تر می‌شود.", "کارینو"),
            DailyQuote("پیشرفت آرام هم پیشرفت است.", "کارینو"),
        )
    }
}
