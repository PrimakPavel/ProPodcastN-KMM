package com.propodcast.app.core.designsystem.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.lerp

private const val THEME_CHANGE_DURATION_MS = 300

/**
 * [target] with a smooth change: when it switches (light ↔ dark), every color blends from the old palette to
 * the new one. One progress value drives all 48 colors instead of 48 separate animations. The first palette
 * shows at once, without animation.
 */
@Composable
internal fun animateColorScheme(target: ColorScheme): ColorScheme {
    var start by remember { mutableStateOf(target) }
    var stop by remember { mutableStateOf(target) }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(target) {
        if (target === stop) return@LaunchedEffect
        // Start from what is on screen now, so a switch in the middle of an animation does not jump.
        start = lerp(start, stop, progress.value)
        stop = target
        progress.snapTo(0f)
        progress.animateTo(1f, tween(THEME_CHANGE_DURATION_MS))
    }
    return lerp(start, stop, progress.value)
}

private fun lerp(start: ColorScheme, stop: ColorScheme, fraction: Float): ColorScheme = when (fraction) {
    0f -> start
    1f -> stop
    else -> stop.copy(
    primary = lerp(start.primary, stop.primary, fraction),
    onPrimary = lerp(start.onPrimary, stop.onPrimary, fraction),
    primaryContainer = lerp(start.primaryContainer, stop.primaryContainer, fraction),
    onPrimaryContainer = lerp(start.onPrimaryContainer, stop.onPrimaryContainer, fraction),
    inversePrimary = lerp(start.inversePrimary, stop.inversePrimary, fraction),
    secondary = lerp(start.secondary, stop.secondary, fraction),
    onSecondary = lerp(start.onSecondary, stop.onSecondary, fraction),
    secondaryContainer = lerp(start.secondaryContainer, stop.secondaryContainer, fraction),
    onSecondaryContainer = lerp(start.onSecondaryContainer, stop.onSecondaryContainer, fraction),
    tertiary = lerp(start.tertiary, stop.tertiary, fraction),
    onTertiary = lerp(start.onTertiary, stop.onTertiary, fraction),
    tertiaryContainer = lerp(start.tertiaryContainer, stop.tertiaryContainer, fraction),
    onTertiaryContainer = lerp(start.onTertiaryContainer, stop.onTertiaryContainer, fraction),
    background = lerp(start.background, stop.background, fraction),
    onBackground = lerp(start.onBackground, stop.onBackground, fraction),
    surface = lerp(start.surface, stop.surface, fraction),
    onSurface = lerp(start.onSurface, stop.onSurface, fraction),
    surfaceVariant = lerp(start.surfaceVariant, stop.surfaceVariant, fraction),
    onSurfaceVariant = lerp(start.onSurfaceVariant, stop.onSurfaceVariant, fraction),
    surfaceTint = lerp(start.surfaceTint, stop.surfaceTint, fraction),
    inverseSurface = lerp(start.inverseSurface, stop.inverseSurface, fraction),
    inverseOnSurface = lerp(start.inverseOnSurface, stop.inverseOnSurface, fraction),
    error = lerp(start.error, stop.error, fraction),
    onError = lerp(start.onError, stop.onError, fraction),
    errorContainer = lerp(start.errorContainer, stop.errorContainer, fraction),
    onErrorContainer = lerp(start.onErrorContainer, stop.onErrorContainer, fraction),
    outline = lerp(start.outline, stop.outline, fraction),
    outlineVariant = lerp(start.outlineVariant, stop.outlineVariant, fraction),
    scrim = lerp(start.scrim, stop.scrim, fraction),
    surfaceBright = lerp(start.surfaceBright, stop.surfaceBright, fraction),
    surfaceDim = lerp(start.surfaceDim, stop.surfaceDim, fraction),
    surfaceContainer = lerp(start.surfaceContainer, stop.surfaceContainer, fraction),
    surfaceContainerHigh = lerp(start.surfaceContainerHigh, stop.surfaceContainerHigh, fraction),
    surfaceContainerHighest = lerp(start.surfaceContainerHighest, stop.surfaceContainerHighest, fraction),
    surfaceContainerLow = lerp(start.surfaceContainerLow, stop.surfaceContainerLow, fraction),
    surfaceContainerLowest = lerp(start.surfaceContainerLowest, stop.surfaceContainerLowest, fraction),
    primaryFixed = lerp(start.primaryFixed, stop.primaryFixed, fraction),
    primaryFixedDim = lerp(start.primaryFixedDim, stop.primaryFixedDim, fraction),
    onPrimaryFixed = lerp(start.onPrimaryFixed, stop.onPrimaryFixed, fraction),
    onPrimaryFixedVariant = lerp(start.onPrimaryFixedVariant, stop.onPrimaryFixedVariant, fraction),
    secondaryFixed = lerp(start.secondaryFixed, stop.secondaryFixed, fraction),
    secondaryFixedDim = lerp(start.secondaryFixedDim, stop.secondaryFixedDim, fraction),
    onSecondaryFixed = lerp(start.onSecondaryFixed, stop.onSecondaryFixed, fraction),
    onSecondaryFixedVariant = lerp(start.onSecondaryFixedVariant, stop.onSecondaryFixedVariant, fraction),
    tertiaryFixed = lerp(start.tertiaryFixed, stop.tertiaryFixed, fraction),
    tertiaryFixedDim = lerp(start.tertiaryFixedDim, stop.tertiaryFixedDim, fraction),
    onTertiaryFixed = lerp(start.onTertiaryFixed, stop.onTertiaryFixed, fraction),
    onTertiaryFixedVariant = lerp(start.onTertiaryFixedVariant, stop.onTertiaryFixedVariant, fraction),
    )
}
