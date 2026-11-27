package com.propodcast.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.propodcast.app.core.data.repository.ApiQuotaRepository
import com.propodcast.app.core.data.repository.FavoritesRepository
import com.propodcast.app.core.data.repository.PodcastRepository
import com.propodcast.app.core.di.DataStoreQualifiers
import org.koin.compose.koinInject
import androidx.datastore.preferences.core.intPreferencesKey
import com.propodcast.app.core.common.apiCall
import com.propodcast.app.core.network.dto.toModel
import kotlinx.coroutines.flow.first
import com.propodcast.app.core.network.ListenNotesApi
import com.propodcast.app.resources.Res
import com.propodcast.app.resources.app_name
import com.propodcast.app.resources.greeting
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.model.ThemeMode
import com.propodcast.app.core.ui.SystemAppearanceEffect
import com.propodcast.app.resources.download_state_downloading
import com.propodcast.app.resources.episodes_count
import com.propodcast.app.resources.ic_download
import com.propodcast.app.resources.ic_download_done
import com.propodcast.app.resources.ic_filter_list
import com.propodcast.app.resources.ic_forward
import com.propodcast.app.resources.ic_pause
import com.propodcast.app.resources.ic_play
import com.propodcast.app.resources.ic_replay
import com.propodcast.app.resources.settings_theme_dark
import com.propodcast.app.resources.settings_theme_light
import com.propodcast.app.resources.settings_theme_system
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun App() {
    val preferences: UserPreferencesRepository = koinInject()
    // Null until DataStore answers (a few ms): drawing nothing avoids a flash of the wrong theme.
    val themeMode by preferences.themeMode.collectAsStateWithLifecycle(initialValue = null)
    val mode = themeMode ?: return
    val darkTheme = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    SystemAppearanceEffect(mode, darkTheme)
    val scope = rememberCoroutineScope()
    ProPodcastTheme(darkTheme = darkTheme) {
        Surface(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.safeDrawingPadding().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(Res.string.greeting, platformName()), style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(24.dp))
                StorageCheck()
                Spacer(Modifier.height(24.dp))
                ResourcesCheck(mode, onModeChange = { scope.launch { preferences.setThemeMode(it) } })
            }
        }
    }
}

/** Temporary check of the design system stage: theme switch, drawables, Material icons, plurals, formats. */
@Composable
private fun ResourcesCheck(mode: ThemeMode, onModeChange: (ThemeMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeMode.entries.forEach { entry ->
            FilterChip(
                selected = entry == mode,
                onClick = { onModeChange(entry) },
                label = {
                    Text(
                        stringResource(
                            when (entry) {
                                ThemeMode.SYSTEM -> Res.string.settings_theme_system
                                ThemeMode.LIGHT -> Res.string.settings_theme_light
                                ThemeMode.DARK -> Res.string.settings_theme_dark
                            },
                        ),
                    )
                },
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(Res.drawable.ic_play, Res.drawable.ic_pause, Res.drawable.ic_replay, Res.drawable.ic_forward,
            Res.drawable.ic_download, Res.drawable.ic_download_done, Res.drawable.ic_filter_list)
            .forEach { Icon(painterResource(it), contentDescription = null) }
        Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Icon(Icons.Filled.Search, contentDescription = null)
    }
    Spacer(Modifier.height(12.dp))
    Text(
        pluralStringResource(Res.plurals.episodes_count, 1, 1) + " · " +
            pluralStringResource(Res.plurals.episodes_count, 12, 12) + " · " +
            stringResource(Res.string.download_state_downloading, 42),
        style = MaterialTheme.typography.bodyMedium,
    )
}

/**
 * Temporary check of the network, data and DI stages (everything comes from Koin): counts launches in DataStore, loads best podcasts
 * through the repository (genres via the on-disk cache) and adds the next one to favorites in Room.
 * Both numbers grow across restarts when storage works.
 */
@Composable
private fun StorageCheck() {
    val userPreferences: DataStore<Preferences> = koinInject(DataStoreQualifiers.UserPreferences)
    val api: ListenNotesApi = koinInject()
    val podcasts: PodcastRepository = koinInject()
    val favorites: FavoritesRepository = koinInject()
    val quota: ApiQuotaRepository = koinInject()
    var text by remember { mutableStateOf("Loading…") }
    LaunchedEffect(Unit) {
        val launches = userPreferences.edit { prefs ->
            prefs[LAUNCHES] = (prefs[LAUNCHES] ?: 0) + 1
        }[LAUNCHES]
        val genres = podcasts.genres().fold({ "${it.size} genres" }, { "genres: ${it.message}" })
        text = apiCall { api.bestPodcasts() }.fold(
            onSuccess = { response ->
                val stored = favorites.favoritePodcastIds.first()
                response.podcasts.map { it.toModel() }.firstOrNull { it.id !in stored }
                    ?.let { favorites.setPodcastFavorite(it, favorite = true) }
                val favoriteTitles = favorites.favoritePodcasts.first().map { it.title }
                val used = quota.quota.value?.let { "API quota: ${it.used} of ${it.freeQuota}" } ?: "API quota: unknown"
                "Launch #$launches (DataStore), $genres (cache)\n$used\n\n" +
                    "Favorites in Room (${favoriteTitles.size}):\n" + favoriteTitles.take(5).joinToString("\n")
            },
            onFailure = { "Error: ${it.message}" },
        )
    }
    Text(text, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
}

private val LAUNCHES = intPreferencesKey("debug_launch_count")
