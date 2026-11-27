package com.propodcast.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.propodcast.app.core.analytics.AnalyticsTracker
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.model.ThemeMode
import com.propodcast.app.core.ui.SystemAppearanceEffect
import org.koin.compose.koinInject

/** Entry point shared by Android (`MainActivity`) and iOS (`MainViewController`). */
@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .crossfade(true)
            .build()
    }
    val preferences: UserPreferencesRepository = koinInject()
    val analytics: AnalyticsTracker = koinInject()
    // Null until DataStore answers (a few ms): drawing nothing avoids a flash of the wrong theme.
    val themeMode by preferences.themeMode.collectAsStateWithLifecycle(initialValue = null)
    val mode = themeMode ?: return
    val darkTheme = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    SystemAppearanceEffect(mode, darkTheme)
    ProPodcastTheme(darkTheme = darkTheme) {
        ProPodcastApp(onScreenView = analytics::logScreen)
    }
}
