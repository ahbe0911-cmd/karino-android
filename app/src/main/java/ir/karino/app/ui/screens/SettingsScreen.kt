package ir.karino.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.karino.app.BuildConfig
import ir.karino.app.data.local.CategoryEntity
import ir.karino.app.domain.model.ThemeMode
import ir.karino.app.ui.KarinoUiState
import ir.karino.app.util.toPersianDigits

@Composable
fun SettingsScreen(
    state: KarinoUiState,
    onThemeModeChange: (ThemeMode) -> Unit,
    onShowQuoteChange: (Boolean) -> Unit,
    onShowCompletedChange: (Boolean) -> Unit,
    onAddCategory: () -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onDeleteCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp)
            .padding(bottom = 100.dp),
    ) {
        Text("تنظیمات", style = MaterialTheme.typography.headlineSmall)
        Text(
            "کارینو را مطابق سلیقهٔ خودتان تنظیم کنید",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SettingsTitle(Icons.Outlined.Palette, "ظاهر")
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp)) {
                Text("حالت نمایش", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = state.settings.themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                            label = { Text(mode.title) },
                        )
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                SettingSwitch(
                    title = "جملهٔ روز",
                    subtitle = "نمایش جملهٔ کوتاه در صفحهٔ امروز",
                    checked = state.settings.showDailyQuote,
                    onCheckedChange = onShowQuoteChange,
                )
                SettingSwitch(
                    title = "کارهای انجام‌شده",
                    subtitle = "نمایش کارهای تیک‌خورده در فهرست‌ها",
                    checked = state.settings.showCompletedTasks,
                    onCheckedChange = onShowCompletedChange,
                )
            }
        }

        SettingsTitle(Icons.Outlined.Category, "دسته‌بندی‌ها")
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                state.categories.forEachIndexed { index, category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(category.colorArgb)),
                        )
                        Text(
                            category.name,
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .weight(1f),
                        )
                        IconButton(onClick = { onDeleteCategory(category) }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "حذف ${category.name}")
                        }
                    }
                    if (index != state.categories.lastIndex) HorizontalDivider()
                }
                TextButton(onClick = onAddCategory, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Text("  دسته‌بندی تازه")
                }
            }
        }

        SettingsTitle(Icons.Outlined.Archive, "پشتیبان‌گیری")
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "یک فایل JSON از تمام کارها و دسته‌بندی‌ها بسازید یا اطلاعات قبلی را بازیابی کنید.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onExport, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Upload, contentDescription = null)
                        Text("  تهیه پشتیبان")
                    }
                    OutlinedButton(onClick = onImport, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Download, contentDescription = null)
                        Text("  بازیابی")
                    }
                }
            }
        }

        SettingsTitle(Icons.Outlined.Lock, "حریم خصوصی")
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Text(
                "کارینو آفلاین است، حساب کاربری نمی‌خواهد و مجوز اینترنت ندارد. اطلاعات شما فقط روی گوشی ذخیره می‌شود.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        if (state.completedCount > 0) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onDeleteCompleted,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
                Text("  پاک‌کردن ${state.completedCount} کار انجام‌شده".toPersianDigits())
            }
        }

        SettingsTitle(Icons.Outlined.Info, "درباره")
        Text(
            "کارینو ${BuildConfig.VERSION_NAME}  •  ساخته‌شده برای مدیریت سادهٔ کارهای روزمره",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(top = 22.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp),
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
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(CATEGORY_COLORS.first()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("دسته‌بندی تازه") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(30) },
                    label = { Text("نام دسته‌بندی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(14.dp))
                Text("رنگ", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CATEGORY_COLORS.forEach { color ->
                        Spacer(
                            Modifier
                                .size(if (selectedColor == color) 34.dp else 30.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .clickable { selectedColor = color },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name, selectedColor) },
            ) { Text("ساخت") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}

private val CATEGORY_COLORS = listOf(
    0xFF17A7A0.toInt(),
    0xFF4F75D8.toInt(),
    0xFFF29B38.toInt(),
    0xFF8A63D2.toInt(),
    0xFFE45D75.toInt(),
    0xFF39956D.toInt(),
)
