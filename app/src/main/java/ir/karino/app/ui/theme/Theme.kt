package ir.karino.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KarinoDarkColors = darkColorScheme(
    primary = ActionTeal,
    onPrimary = OnActionTeal,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = MainText,
    secondary = ActionTeal,
    onSecondary = OnActionTeal,
    secondaryContainer = RaisedSurface,
    onSecondaryContainer = MainText,
    background = AppBackground,
    onBackground = MainText,
    surface = CardBackground,
    onSurface = MainText,
    surfaceVariant = RaisedSurface,
    onSurfaceVariant = MutedText,
    outline = Outline,
    outlineVariant = OutlineSoft,
    error = ErrorSoft,
    onError = ColorTokens.OnError,
)

private object ColorTokens {
    val OnError = androidx.compose.ui.graphics.Color(0xFF690005)
}

@Composable
fun KarinoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KarinoDarkColors,
        typography = KarinoTypography,
        content = content,
    )
}
