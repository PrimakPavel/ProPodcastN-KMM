package com.propodcast.app.core.ui

import androidx.compose.runtime.Composable
import com.propodcast.app.core.model.ThemeMode

/**
 * Makes the system chrome follow the app theme chosen in Settings, not the device one:
 * status and navigation bar icons on Android, the window's light/dark style (status bar included) on iOS.
 */
@Composable
expect fun SystemAppearanceEffect(mode: ThemeMode, darkTheme: Boolean)
