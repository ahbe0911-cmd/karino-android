package ir.karino.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import ir.karino.app.domain.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = LightSurface,
    primaryContainer = TealLight,
    onPrimaryContainer = Navy,
    secondary = Navy,
    onSecondary = LightSurface,
    secondaryContainer = ColorTokens.LightSecondaryContainer,
    onSecondaryContainer = Navy,
    tertiary = WarmGold,
    onTertiary = Navy,
    background = LightBackground,
    onBackground = NavyDark,
    surface = LightSurface,
    onSurface = NavyDark,
    surfaceVariant = ColorTokens.LightSurfaceVariant,
    onSurfaceVariant = ColorTokens.LightOnSurfaceVariant,
    error = ErrorRed,
)

private val DarkColors = darkColorScheme(
    primary = TealDark,
    onPrimary = NavyDark,
    primaryContainer = ColorTokens.DarkPrimaryContainer,
    onPrimaryContainer = TealLight,
    secondary = ColorTokens.DarkSecondary,
    onSecondary = NavyDark,
    secondaryContainer = ColorTokens.DarkSecondaryContainer,
    onSecondaryContainer = ColorTokens.DarkOnSecondaryContainer,
    tertiary = WarmGold,
    onTertiary = NavyDark,
    background = DarkBackground,
    onBackground = ColorTokens.DarkOnBackground,
    surface = DarkSurface,
    onSurface = ColorTokens.DarkOnBackground,
    surfaceVariant = ColorTokens.DarkSurfaceVariant,
    onSurfaceVariant = ColorTokens.DarkOnSurfaceVariant,
)

private object ColorTokens {
    val LightSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFDDECF1)
    val LightSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFE4ECEB)
    val LightOnSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF405456)
    val DarkPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF075C5A)
    val DarkSecondary = androidx.compose.ui.graphics.Color(0xFFB5CBD2)
    val DarkSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF304A53)
    val DarkOnSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFD5EBF2)
    val DarkOnBackground = androidx.compose.ui.graphics.Color(0xFFDCE9EC)
    val DarkSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF283B40)
    val DarkOnSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFBDCBCE)
}

@Composable
fun KarinoTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = KarinoTypography,
        content = content,
    )
}
