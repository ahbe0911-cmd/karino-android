package ir.karino.app.data.local

import androidx.room.TypeConverter
import ir.karino.app.domain.model.RepeatRule
import ir.karino.app.domain.model.TaskPriority

class Converters {
    @TypeConverter
    fun priorityToString(value: TaskPriority): String = value.name

    @TypeConverter
    fun stringToPriority(value: String): TaskPriority =
        runCatching { TaskPriority.valueOf(value) }.getOrDefault(TaskPriority.NORMAL)

    @TypeConverter
    fun repeatRuleToString(value: RepeatRule): String = value.name

    @TypeConverter
    fun stringToRepeatRule(value: String): RepeatRule =
        runCatching { RepeatRule.valueOf(value) }.getOrDefault(RepeatRule.NONE)
}
