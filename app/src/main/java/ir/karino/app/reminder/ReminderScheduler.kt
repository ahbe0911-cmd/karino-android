package ir.karino.app.reminder

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.karino.app.data.local.TaskEntity
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(context)
    }

    fun schedule(task: TaskEntity) {
        val reminderAt = task.reminderAt
        if (task.isCompleted || reminderAt == null || reminderAt <= System.currentTimeMillis()) {
            cancel(task.id)
            return
        }

        val input = Data.Builder()
            .putLong(ReminderWorker.KEY_TASK_ID, task.id)
            .putString(ReminderWorker.KEY_TITLE, task.title)
            .putString(ReminderWorker.KEY_NOTE, task.note)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(input)
            .setInitialDelay(reminderAt - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .addTag(tag(task.id))
            .build()

        workManager.enqueueUniqueWork(
            workName(task.id),
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun cancel(taskId: Long) {
        if (taskId > 0) workManager.cancelUniqueWork(workName(taskId))
    }

    private fun workName(taskId: Long) = "karino_reminder_$taskId"
    private fun tag(taskId: Long) = "karino_task_$taskId"
}
