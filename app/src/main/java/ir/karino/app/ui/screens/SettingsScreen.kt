package ir.karino.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.karino.app.BuildConfig
import ir.karino.app.ui.KarinoUiState
import ir.karino.app.util.toPersianDigits

@Composable
fun SettingsScreen(
    state: KarinoUiState,
    onShowQuoteChange: (Boolean) -> Unit,
    onShowCompletedChange: (Boolean) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onDeleteCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 18.dp,
            bottom = 32.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "title") {
            Text(
                text = "تنظیمات",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        item(key = "display-label") {
            SectionLabel("نمایش")
        }
        item(key = "display") {
            SettingsCard {
                SettingSwitch(
                    title = "جملهٔ کوتاه",
                    subtitle = "نمایش جملهٔ کوچک بالای یادداشت‌ها",
                    checked = state.settings.showDailyQuote,
                    onCheckedChange = onShowQuoteChange,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingSwitch(
                    title = "انجام‌شده‌ها",
                    subtitle = "یادداشت‌های تیک‌خورده در فهرست بمانند",
                    checked = state.settings.showCompletedTasks,
                    onCheckedChange = onShowCompletedChange,
                )
            }
        }

        item(key = "backup-label") {
            SectionLabel("پشتیبان")
        }
        item(key = "backup") {
            SettingsCard {
                Text(
                    text = "یادداشت‌ها را در یک فایل نگه دار یا دوباره بازیابی کن.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onExport, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Upload, contentDescription = null)
                        Text("  پشتیبان")
                    }
                    OutlinedButton(onClick = onImport, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Download, contentDescription = null)
                        Text("  بازیابی")
                    }
                }
            }
        }

        item(key = "privacy-label") {
            SectionLabel("حریم خصوصی")
        }
        item(key = "privacy") {
            SettingsCard {
                Text(
                    text = "کارینو آفلاین است و اطلاعات فقط روی گوشی تو می‌ماند.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        if (state.completedCount > 0) {
            item(key = "delete-completed") {
                OutlinedButton(
                    onClick = onDeleteCompleted,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
                    Text(
                        "  پاک‌کردن ${state.completedCount} یادداشت انجام‌شده".toPersianDigits(),
                    )
                }
            }
        }

        item(key = "version") {
            Text(
                text = "کارینو ${BuildConfig.VERSION_NAME}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(top = 6.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            content = content,
        )
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
