package ir.karino.app.data.backup

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.karino.app.data.local.CategoryEntity
import ir.karino.app.data.local.TaskEntity
import ir.karino.app.data.repository.BackupSnapshot
import ir.karino.app.domain.model.RepeatRule
import ir.karino.app.domain.model.TaskPriority
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun export(uri: Uri, snapshot: BackupSnapshot) = withContext(Dispatchers.IO) {
        val root = JSONObject().apply {
            put("schemaVersion", SCHEMA_VERSION)
            put("app", "Karino")
            put("createdAt", System.currentTimeMillis())
            put("categories", JSONArray().apply {
                snapshot.categories.forEach { category ->
                    put(JSONObject().apply {
                        put("id", category.id)
                        put("name", category.name)
                        put("colorArgb", category.colorArgb)
                        put("createdAt", category.createdAt)
                    })
                }
            })
            put("tasks", JSONArray().apply {
                snapshot.tasks.forEach { task ->
                    put(JSONObject().apply {
                        put("id", task.id)
                        put("title", task.title)
                        put("note", task.note)
                        putNullable("categoryId", task.categoryId)
                        put("priority", task.priority.name)
                        putNullable("dueAt", task.dueAt)
                        putNullable("reminderAt", task.reminderAt)
                        put("repeatRule", task.repeatRule.name)
                        put("isCompleted", task.isCompleted)
                        putNullable("completedAt", task.completedAt)
                        put("sortOrder", task.sortOrder)
                        put("createdAt", task.createdAt)
                        put("updatedAt", task.updatedAt)
                    })
                }
            })
        }

        val output = context.contentResolver.openOutputStream(uri, "wt")
            ?: error("امکان باز کردن فایل مقصد وجود ندارد.")
        output.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
            writer.write(root.toString(2))
        }
    }

    suspend fun import(uri: Uri): BackupSnapshot = withContext(Dispatchers.IO) {
        val input = context.contentResolver.openInputStream(uri)
            ?: error("امکان خواندن فایل انتخاب‌شده وجود ندارد.")
        val text = input.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        require(text.toByteArray(StandardCharsets.UTF_8).size <= MAX_BACKUP_BYTES) {
            "حجم فایل پشتیبان بیش از حد مجاز است."
        }

        val root = JSONObject(text)
        require(root.optInt("schemaVersion", -1) == SCHEMA_VERSION) {
            "نسخهٔ فایل پشتیبان پشتیبانی نمی‌شود."
        }

        val categoriesJson = root.getJSONArray("categories")
        val categories = buildList {
            for (index in 0 until categoriesJson.length()) {
                val item = categoriesJson.getJSONObject(index)
                add(
                    CategoryEntity(
                        id = item.getLong("id"),
                        name = item.getString("name").trim(),
                        colorArgb = item.getInt("colorArgb"),
                        createdAt = item.getLong("createdAt"),
                    ),
                )
            }
        }
        require(categories.all { it.id > 0 && it.name.isNotBlank() }) {
            "یکی از دسته‌بندی‌های فایل معتبر نیست."
        }
        require(categories.map { it.id }.distinct().size == categories.size) {
            "شناسهٔ دسته‌بندی تکراری است."
        }
        val categoryIds = categories.mapTo(mutableSetOf()) { it.id }

        val tasksJson = root.getJSONArray("tasks")
        val tasks = buildList {
            for (index in 0 until tasksJson.length()) {
                val item = tasksJson.getJSONObject(index)
                val categoryId = item.optionalLong("categoryId")
                require(categoryId == null || categoryId in categoryIds) {
                    "دسته‌بندی یکی از کارها در فایل وجود ندارد."
                }
                add(
                    TaskEntity(
                        id = item.getLong("id"),
                        title = item.getString("title").trim(),
                        note = item.optString("note", "").trim(),
                        categoryId = categoryId,
                        priority = enumValueOrDefault(
                            item.optString("priority"),
                            TaskPriority.NORMAL,
                        ),
                        dueAt = item.optionalLong("dueAt"),
                        reminderAt = item.optionalLong("reminderAt"),
                        repeatRule = enumValueOrDefault(
                            item.optString("repeatRule"),
                            RepeatRule.NONE,
                        ),
                        isCompleted = item.optBoolean("isCompleted", false),
                        completedAt = item.optionalLong("completedAt"),
                        sortOrder = item.optLong("sortOrder", index.toLong()),
                        createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = item.optLong("updatedAt", System.currentTimeMillis()),
                    ),
                )
            }
        }
        require(tasks.all { it.id > 0 && it.title.isNotBlank() }) {
            "یکی از کارهای فایل معتبر نیست."
        }
        require(tasks.map { it.id }.distinct().size == tasks.size) {
            "شناسهٔ کار تکراری است."
        }
        BackupSnapshot(tasks = tasks, categories = categories)
    }

    private fun JSONObject.putNullable(key: String, value: Long?) {
        put(key, value ?: JSONObject.NULL)
    }

    private fun JSONObject.optionalLong(key: String): Long? =
        if (!has(key) || isNull(key)) null else getLong(key)

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String, fallback: T): T =
        runCatching { enumValueOf<T>(value) }.getOrDefault(fallback)

    companion object {
        private const val SCHEMA_VERSION = 1
        private const val MAX_BACKUP_BYTES = 5 * 1024 * 1024
    }
}
