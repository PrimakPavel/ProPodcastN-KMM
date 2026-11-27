package com.propodcast.app.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.propodcast.app.core.model.ThemeMode
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

@Composable
actual fun SystemAppearanceEffect(mode: ThemeMode, darkTheme: Boolean) {
    DisposableEffect(mode) {
        val style = when (mode) {
            // Unspecified: back to the device setting, which isSystemInDarkTheme() then reports.
            ThemeMode.SYSTEM -> UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified
            ThemeMode.LIGHT -> UIUserInterfaceStyle.UIUserInterfaceStyleLight
            ThemeMode.DARK -> UIUserInterfaceStyle.UIUserInterfaceStyleDark
        }
        UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .flatMap { it.windows.filterIsInstance<UIWindow>() }
            .forEach { it.overrideUserInterfaceStyle = style }
        onDispose {}
    }
}
