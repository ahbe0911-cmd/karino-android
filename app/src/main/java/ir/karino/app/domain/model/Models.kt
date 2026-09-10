package ir.karino.app.domain.model

enum class TaskPriority(val title: String) {
    LOW("کم"),
    NORMAL("عادی"),
    HIGH("مهم"),
    URGENT("فوری"),
}

enum class RepeatRule(val title: String) {
    NONE("بدون تکرار"),
    DAILY("هر روز"),
    WEEKLY("هر هفته"),
    MONTHLY("هر ماه"),
}

enum class TaskFilter(val title: String) {
    TODAY("امروز"),
    UPCOMING("آینده"),
    ALL("همه"),
    COMPLETED("انجام‌شده"),
}

enum class ThemeMode(val title: String) {
    SYSTEM("خودکار"),
    LIGHT("روشن"),
    DARK("تیره"),
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showDailyQuote: Boolean = true,
    val showCompletedTasks: Boolean = true,
)

data class TaskDraft(
    val id: Long = 0,
    val title: String = "",
    val note: String = "",
    val categoryId: Long? = null,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val dueAt: Long? = null,
    val reminderAt: Long? = null,
    val repeatRule: RepeatRule = RepeatRule.NONE,
)
