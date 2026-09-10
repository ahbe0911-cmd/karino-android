package ir.karino.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.karino.app.domain.model.AppSettings
import ir.karino.app.domain.model.ThemeMode
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "karino_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
        val showDailyQuote = booleanPreferencesKey("show_daily_quote")
        val showCompletedTasks = booleanPreferencesKey("show_completed_tasks")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw error
        }
        .map { preferences ->
            AppSettings(
                themeMode = preferences[Keys.themeMode]
                    ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                    ?: ThemeMode.SYSTEM,
                showDailyQuote = preferences[Keys.showDailyQuote] ?: true,
                showCompletedTasks = preferences[Keys.showCompletedTasks] ?: true,
            )
        }

    suspend fun setThemeMode(value: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.themeMode] = value.name }
    }

    suspend fun setShowDailyQuote(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.showDailyQuote] = value }
    }

    suspend fun setShowCompletedTasks(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.showCompletedTasks] = value }
    }
}
