package ir.karino.app.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.karino.app.data.local.TaskEntity
import ir.karino.app.reminder.ReminderWorker
import ir.karino.app.ui.components.TaskEditorSheet
import ir.karino.app.ui.screens.HomeScreen
import ir.karino.app.ui.screens.SettingsScreen
import ir.karino.app.ui.theme.KarinoTheme
import kotlinx.coroutines.launch

private enum class RootSection(val title: String) {
    NOTES("یادداشت‌ها"),
    SETTINGS("تنظیمات"),
}

@Composable
fun KarinoRoot(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    KarinoTheme {
        CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Rtl,
        ) {
            KarinoScaffold(viewModel = viewModel, state = state)
        }
    }
}

@Composable
private fun KarinoScaffold(
    viewModel: MainViewModel,
    state: KarinoUiState,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var section by remember { mutableStateOf(RootSection.NOTES) }
    var showTaskEditor by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var confirmDeleteCompleted by remember { mutableStateOf(false) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let(viewModel::exportBackup) }

    val openBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> pendingImportUri = uri }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "بدون اجازهٔ اعلان، یادآور ذخیره می‌شود اما نمایش داده نخواهد شد.",
                )
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(state.allTasks, activity) {
        val requestedId = activity?.intent?.getLongExtra(ReminderWorker.EXTRA_TASK_ID, 0L) ?: 0L
        if (requestedId > 0) {
            state.allTasks.firstOrNull { it.id == requestedId }?.let { task ->
                editingTask = task
                showTaskEditor = true
                activity?.intent?.removeExtra(ReminderWorker.EXTRA_TASK_ID)
            }
        }
    }

    fun openNewTask() {
        editingTask = null
        showTaskEditor = true
    }

    fun openTask(task: TaskEntity) {
        editingTask = task
        showTaskEditor = true
    }

    fun deleteWithUndo(task: TaskEntity) {
        scope.launch {
            if (viewModel.deleteTask(task)) {
                val result = snackbarHostState.showSnackbar(
                    message = "یادداشت حذف شد.",
                    actionLabel = "بازگردانی",
                    withDismissAction = true,
                    duration = SnackbarDuration.Long,
                )
                if (result == SnackbarResult.ActionPerformed) viewModel.restoreTask(task)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                RootSection.entries.forEach { item ->
                    NavigationBarItem(
                        selected = section == item,
                        onClick = { section = item },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        icon = {
                            Icon(
                                imageVector = when (item) {
                                    RootSection.NOTES -> Icons.AutoMirrored.Outlined.Notes
                                    RootSection.SETTINGS -> Icons.Outlined.Settings
                                },
                                contentDescription = item.title,
                            )
                        },
                        label = { Text(item.title) },
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Start,
        floatingActionButton = {
            if (section == RootSection.NOTES) {
                FloatingActionButton(
                    onClick = ::openNewTask,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "افزودن یادداشت")
                }
            }
        },
    ) { contentPadding ->
        Box(Modifier.padding(contentPadding)) {
            when (section) {
                RootSection.NOTES -> HomeScreen(
                    state = state,
                    onCheckTask = viewModel::setCompleted,
                    onEditTask = ::openTask,
                )

                RootSection.SETTINGS -> SettingsScreen(
                    state = state,
                    onShowQuoteChange = viewModel::setShowDailyQuote,
                    onShowCompletedChange = viewModel::setShowCompletedTasks,
                    onExport = { createBackupLauncher.launch("karino-backup.json") },
                    onImport = { openBackupLauncher.launch(arrayOf("application/json", "text/plain")) },
                    onDeleteCompleted = { confirmDeleteCompleted = true },
                )
            }
        }
    }

    if (showTaskEditor) {
        val taskBeingEdited = editingTask
        TaskEditorSheet(
            task = taskBeingEdited,
            onDismiss = { showTaskEditor = false },
            onSave = {
                viewModel.saveTask(it)
                showTaskEditor = false
            },
            onDelete = taskBeingEdited?.let { task ->
                {
                    showTaskEditor = false
                    deleteWithUndo(task)
                }
            },
            onReminderPermissionNeeded = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
        )
    }

    pendingImportUri?.let { uri ->
        ConfirmDialog(
            title = "بازیابی فایل پشتیبان؟",
            message = "تمام یادداشت‌های فعلی با اطلاعات داخل فایل جایگزین می‌شوند.",
            confirmText = "بازیابی",
            onDismiss = { pendingImportUri = null },
            onConfirm = {
                viewModel.importBackup(uri)
                pendingImportUri = null
            },
        )
    }

    if (confirmDeleteCompleted) {
        ConfirmDialog(
            title = "پاک‌کردن یادداشت‌های انجام‌شده؟",
            message = "این کار قابل بازگردانی نیست. بهتر است پیش از آن فایل پشتیبان بگیرید.",
            confirmText = "پاک‌کردن",
            onDismiss = { confirmDeleteCompleted = false },
            onConfirm = {
                viewModel.deleteCompleted()
                confirmDeleteCompleted = false
            },
        )
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { Button(onClick = onConfirm) { Text(confirmText) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}
