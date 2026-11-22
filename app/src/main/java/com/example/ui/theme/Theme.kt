package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val RoyalDarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    onPrimary = ObsidianBlack,
    primaryContainer = RoyalBlue,
    onPrimaryContainer = PlatinumText,
    secondary = RoyalBlue,
    onSecondary = PlatinumText,
    secondaryContainer = SapphireElevated,
    onSecondaryContainer = NeonBlue,
    tertiary = SubtleEmerald,
    onTertiary = ObsidianBlack,
    tertiaryContainer = EmeraldContainerDark,
    onTertiaryContainer = EmeraldGlow,
    background = ObsidianBlack,
    onBackground = PlatinumText,
    surface = MidnightSurface,
    onSurface = PlatinumText,
    surfaceVariant = SapphireCard,
    onSurfaceVariant = SilverMuted,
    surfaceContainerLowest = ObsidianBlack,
    surfaceContainerLow = MidnightSurface,
    surfaceContainer = SapphireCard,
    surfaceContainerHigh = SapphireElevated,
    surfaceContainerHighest = Color(0xFF1F2B4A),
    outline = SlateBorder,
    outlineVariant = Color(0xFF28375A),
    error = CrimsonError,
    onError = ObsidianBlack,
    errorContainer = CrimsonContainerDark,
    onErrorContainer = Color(0xFFFFE4E6)
)

private val RoyalLightColorScheme = lightColorScheme(
    primary = RoyalBlueLight,
    onPrimary = Color.White,
    primaryContainer = SoftSapphireContainerLight,
    onPrimaryContainer = RoyalBlueLight,
    secondary = AzureNeonLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0C4A6E),
    tertiary = EmeraldLight,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldContainerLight,
    onTertiaryContainer = Color(0xFF064E3B),
    background = AlabasterLight,
    onBackground = InkDark,
    surface = WhiteSurfaceLight,
    onSurface = InkDark,
    surfaceVariant = SoftSapphireContainerLight,
    onSurfaceVariant = SlateMutedLight,
    surfaceContainerLowest = WhiteSurfaceLight,
    surfaceContainerLow = Color(0xFFF8FAFF),
    surfaceContainer = Color(0xFFF1F5FD),
    surfaceContainerHigh = SoftSapphireContainerLight,
    surfaceContainerHighest = Color(0xFFDCE7FA),
    outline = OutlineLight,
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFBE123C),
    onError = Color.White,
    errorContainer = Color(0xFFFFE4E6),
    onErrorContainer = Color(0xFF881337)
)

@Composable
fun RoyalGptTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> RoyalDarkColorScheme
        else -> RoyalLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RoyalTypography,
        shapes = RoyalShapes,
        content = content
    )
}
