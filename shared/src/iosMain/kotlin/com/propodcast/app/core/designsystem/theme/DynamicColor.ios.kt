package com.propodcast.app.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/** iOS has no wallpaper-based palette: the app's own colors are used. */
@Composable
internal actual fun dynamicColorScheme(darkTheme: Boolean): ColorScheme? = null
