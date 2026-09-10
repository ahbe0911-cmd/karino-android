package ir.karino.app.data.repository

import androidx.room.withTransaction
import ir.karino.app.data.local.CategoryDao
import ir.karino.app.data.local.CategoryEntity
import ir.karino.app.data.local.KarinoDatabase
import ir.karino.app.data.local.TaskDao
import ir.karino.app.data.local.TaskEntity
import ir.karino.app.domain.model.RepeatRule
import ir.karino.app.domain.model.TaskDraft
import ir.karino.app.reminder.ReminderScheduler
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

data class BackupSnapshot(
    val tasks: List<TaskEntity>,
    val categories: List<CategoryEntity>,
)

@Singleton
class TaskRepository @Inject constructor(
    private val database: KarinoDatabase,
    private val taskDao: TaskDao,
    private val categoryDao: CategoryDao,
    private val reminderScheduler: ReminderScheduler,
) {
    val tasks: Flow<List<TaskEntity>> = taskDao.observeAll()
    val categories: Flow<List<CategoryEntity>> = categoryDao.observeAll()

    suspend fun ensureDefaultCategories() {
        if (categoryDao.count() != 0) return
        val now = System.currentTimeMillis()
        categoryDao.insertAll(
            listOf(
                CategoryEntity(name = "شخصی", colorArgb = 0xFF17A7A0.toInt(), createdAt = now),
                CategoryEntity(name = "کار", colorArgb = 0xFF4F75D8.toInt(), createdAt = now + 1),
                CategoryEntity(name = "خرید", colorArgb = 0xFFF29B38.toInt(), createdAt = now + 2),
                CategoryEntity(name = "مطالعه", colorArgb = 0xFF8A63D2.toInt(), createdAt = now + 3),
            ),
        )
    }

    suspend fun save(draft: TaskDraft): TaskEntity {
        require(draft.title.isNotBlank()) { "عنوان کار نمی‌تواند خالی باشد." }
        val now = System.currentTimeMillis()
        val existing = draft.id.takeIf { it > 0 }?.let(taskDao::getById)
        val task = if (existing == null) {
            TaskEntity(
                title = draft.title.trim(),
                note = draft.note.trim(),
                categoryId = draft.categoryId,
                priority = draft.priority,
                dueAt = draft.dueAt,
                reminderAt = draft.reminderAt,
                repeatRule = draft.repeatRule,
                sortOrder = taskDao.nextSortOrder(),
                createdAt = now,
                updatedAt = now,
            )
        } else {
            existing.copy(
                title = draft.title.trim(),
                note = draft.note.trim(),
                categoryId = draft.categoryId,
                priority = draft.priority,
                dueAt = draft.dueAt,
                reminderAt = draft.reminderAt,
                repeatRule = draft.repeatRule,
                updatedAt = now,
            )
        }

        val saved = if (existing == null) {
            task.copy(id = taskDao.insert(task))
        } else {
            taskDao.update(task)
            task
        }
        reminderScheduler.schedule(saved)
        return saved
    }

    suspend fun setCompleted(taskId: Long, completed: Boolean) {
        var updatedTask: TaskEntity? = null
        var nextTask: TaskEntity? = null
        database.withTransaction {
            val current = taskDao.getById(taskId) ?: return@withTransaction
            if (current.isCompleted == completed) return@withTransaction
            val now = System.currentTimeMillis()
            updatedTask = current.copy(
                isCompleted = completed,
                completedAt = if (completed) now else null,
                updatedAt = now,
            ).also { taskDao.update(it) }

            if (completed && current.repeatRule != RepeatRule.NONE && current.dueAt != null) {
                var nextDue = advance(current.dueAt, current.repeatRule)
                while (nextDue <= now) {
                    nextDue = advance(nextDue, current.repeatRule)
                }
                val reminderOffset = current.reminderAt?.let { current.dueAt - it }
                val created = current.copy(
                    id = 0,
                    dueAt = nextDue,
                    reminderAt = reminderOffset?.let { nextDue - it },
                    isCompleted = false,
                    completedAt = null,
                    sortOrder = taskDao.nextSortOrder(),
                    createdAt = now,
                    updatedAt = now,
                )
                nextTask = created.copy(id = taskDao.insert(created))
            }
        }

        updatedTask?.let {
            if (it.isCompleted) reminderScheduler.cancel(it.id) else reminderScheduler.schedule(it)
        }
        nextTask?.let(reminderScheduler::schedule)
    }

    suspend fun delete(task: TaskEntity) {
        taskDao.delete(task)
        reminderScheduler.cancel(task.id)
    }

    suspend fun restore(task: TaskEntity) {
        taskDao.insert(task)
        reminderScheduler.schedule(task)
    }

    suspend fun deleteCompleted() {
        val completed = taskDao.getAll().filter(TaskEntity::isCompleted)
        taskDao.deleteCompleted()
        completed.forEach { reminderScheduler.cancel(it.id) }
    }

    suspend fun addCategory(name: String, colorArgb: Int): Long {
        require(name.isNotBlank()) { "نام دسته‌بندی نمی‌تواند خالی باشد." }
        return categoryDao.insert(
            CategoryEntity(name = name.trim(), colorArgb = colorArgb),
        )
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.delete(category)
    }

    suspend fun snapshot(): BackupSnapshot = database.withTransaction {
        BackupSnapshot(
            tasks = taskDao.getAll(),
            categories = categoryDao.getAll(),
        )
    }

    suspend fun replaceAll(snapshot: BackupSnapshot) {
        taskDao.getAll().forEach { reminderScheduler.cancel(it.id) }
        database.withTransaction {
            taskDao.deleteAll()
            categoryDao.deleteAll()
            categoryDao.insertAll(snapshot.categories)
            taskDao.insertAll(snapshot.tasks)
        }
        snapshot.tasks.filterNot(TaskEntity::isCompleted).forEach(reminderScheduler::schedule)
    }

    private fun advance(epochMillis: Long, rule: RepeatRule): Long {
        val zoned = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
        val next = when (rule) {
            RepeatRule.DAILY -> zoned.plusDays(1)
            RepeatRule.WEEKLY -> zoned.plusWeeks(1)
            RepeatRule.MONTHLY -> zoned.plusMonths(1)
            RepeatRule.NONE -> zoned
        }
        return next.toInstant().toEpochMilli()
    }
}
