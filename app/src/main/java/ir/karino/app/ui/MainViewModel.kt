package ir.karino.app.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.karino.app.data.backup.BackupManager
import ir.karino.app.data.local.TaskEntity
import ir.karino.app.data.preferences.SettingsRepository
import ir.karino.app.data.repository.TaskRepository
import ir.karino.app.domain.model.AppSettings
import ir.karino.app.domain.model.TaskDraft
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DailyQuote(val text: String)

data class KarinoUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val allTasks: List<TaskEntity> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val completedCount: Int = 0,
    val dailyQuote: DailyQuote = DailyQuote("فقط کار بعدی."),
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager,
) : ViewModel() {
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages = _messages.asSharedFlow()

    /*
     * Room already returns tasks in display order. Keeping filtering in this single
     * background transform avoids category/statistics flows and repeated sorting.
     */
    val uiState = combine(
        taskRepository.tasks,
        settingsRepository.settings,
    ) { tasks, settings ->
        val visibleTasks = if (settings.showCompletedTasks) {
            tasks
        } else {
            tasks.filterNot(TaskEntity::isCompleted)
        }
        KarinoUiState(
            tasks = visibleTasks,
            allTasks = tasks,
            settings = settings,
            completedCount = tasks.count(TaskEntity::isCompleted),
            dailyQuote = QUOTES[LocalDate.now().dayOfYear % QUOTES.size],
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = KarinoUiState(),
        )

    fun saveTask(draft: TaskDraft) {
        viewModelScope.launch {
            runCatching { taskRepository.save(draft) }
                .onSuccess {
                    _messages.emit(
                        if (draft.id == 0L) "یادداشت ذخیره شد." else "تغییرات ذخیره شد.",
                    )
                }
                .onFailure { _messages.emit(it.message ?: "ذخیرهٔ یادداشت انجام نشد.") }
        }
    }

    fun setCompleted(task: TaskEntity, completed: Boolean) {
        viewModelScope.launch {
            runCatching { taskRepository.setCompleted(task.id, completed) }
                .onFailure { _messages.emit("تغییر وضعیت یادداشت انجام نشد.") }
        }
    }

    suspend fun deleteTask(task: TaskEntity): Boolean =
        runCatching { taskRepository.delete(task) }
            .onFailure { _messages.emit("حذف یادداشت انجام نشد.") }
            .isSuccess

    suspend fun restoreTask(task: TaskEntity) {
        runCatching { taskRepository.restore(task) }
            .onFailure { _messages.emit("بازگردانی یادداشت انجام نشد.") }
    }

    fun deleteCompleted() {
        viewModelScope.launch {
            runCatching { taskRepository.deleteCompleted() }
                .onSuccess { _messages.emit("یادداشت‌های انجام‌شده پاک شدند.") }
                .onFailure { _messages.emit("پاک‌سازی انجام نشد.") }
        }
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
            }
                .onSuccess { _messages.emit("اطلاعات فایل پشتیبان بازیابی شد.") }
                .onFailure { _messages.emit(it.message ?: "بازیابی فایل انجام نشد.") }
        }
    }

    companion object {
        private val QUOTES = listOf(
            DailyQuote("فقط کار بعدی."),
            DailyQuote("آرام و پیوسته."),
            DailyQuote("یک قدم کافی است."),
            DailyQuote("ساده شروع کن."),
            DailyQuote("امروز سبک‌تر."),
            DailyQuote("کم، ولی انجام‌شده."),
            DailyQuote("ادامه بده."),
        )
    }
}
