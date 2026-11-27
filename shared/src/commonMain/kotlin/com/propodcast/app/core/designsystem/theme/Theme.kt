package com.propodcast.app.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
)

/**
 * Material You colors from the wallpaper (Android 12+); null where the platform has none (older Android, iOS).
 * Must return the same object for the same arguments, so [ProPodcastTheme] can tell a real change.
 */
@Composable
internal expect fun dynamicColorScheme(darkTheme: Boolean): ColorScheme?

@Composable
fun ProPodcastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dynamic = if (dynamicColor) dynamicColorScheme(darkTheme) else null
    // Remembered so the same palette is the same object: only a real change starts the color animation.
    val colorScheme = remember(darkTheme, dynamic) {
        dynamic ?: if (darkTheme) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = animateColorScheme(colorScheme),
        typography = Typography,
        content = content,
    )
}
