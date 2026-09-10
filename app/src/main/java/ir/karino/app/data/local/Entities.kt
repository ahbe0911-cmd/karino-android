package ir.karino.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ir.karino.app.domain.model.RepeatRule
import ir.karino.app.domain.model.TaskPriority

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorArgb: Int,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("categoryId"), Index("dueAt"), Index("isCompleted")],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String = "",
    val categoryId: Long? = null,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val dueAt: Long? = null,
    val reminderAt: Long? = null,
    val repeatRule: RepeatRule = RepeatRule.NONE,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val sortOrder: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
