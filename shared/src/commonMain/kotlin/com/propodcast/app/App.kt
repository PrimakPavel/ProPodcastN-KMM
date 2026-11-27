package com.propodcast.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.propodcast.app.core.common.apiCall
import com.propodcast.app.core.commonwidgets.ExpandableHtmlText
import com.propodcast.app.core.commonwidgets.PodcastListItem
import com.propodcast.app.core.commonwidgets.QuotaBanner
import com.propodcast.app.core.data.repository.ApiQuotaRepository
import com.propodcast.app.core.data.repository.FavoritesRepository
import com.propodcast.app.core.data.repository.PodcastRepository
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.di.DataStoreQualifiers
import com.propodcast.app.core.model.ApiQuota
import com.propodcast.app.core.model.ThemeMode
import com.propodcast.app.core.network.ListenNotesApi
import com.propodcast.app.core.network.dto.toModel
import com.propodcast.app.core.ui.SystemAppearanceEffect
import com.propodcast.app.core.ui.rememberLinkSharer
import com.propodcast.app.resources.Res
import com.propodcast.app.resources.action_share
import com.propodcast.app.resources.app_name
import com.propodcast.app.resources.greeting
import com.propodcast.app.resources.settings_theme_dark
import com.propodcast.app.resources.settings_theme_light
import com.propodcast.app.resources.settings_theme_system
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .crossfade(true)
            .build()
    }
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
            MigrationCheck(mode, onModeChange = { scope.launch { preferences.setThemeMode(it) } })
        }
    }
}

/**
 * Temporary screen until the UI stage is done: checks the stages so far on the device.
 * Storage (DataStore launch counter, Room favorites, cached genres), network (best podcasts, quota),
 * theme switch, and the shared widgets (podcast rows with Coil artwork, HTML text, quota banner, share).
 */
@Composable
private fun MigrationCheck(mode: ThemeMode, onModeChange: (ThemeMode) -> Unit) {
    val userPreferences: DataStore<Preferences> = koinInject(DataStoreQualifiers.UserPreferences)
    val api: ListenNotesApi = koinInject()
    val podcasts: PodcastRepository = koinInject()
    val favorites: FavoritesRepository = koinInject()
    val quota: ApiQuotaRepository = koinInject()
    val share = rememberLinkSharer()
    val shareTitle = stringResource(Res.string.action_share)
    val scope = rememberCoroutineScope()

    var status by remember { mutableStateOf("Loading…") }
    val favoritePodcasts by favorites.favoritePodcasts.collectAsStateWithLifecycle(initialValue = emptyList())
    LaunchedEffect(Unit) {
        val launches = userPreferences.edit { prefs -> prefs[LAUNCHES] = (prefs[LAUNCHES] ?: 0) + 1 }[LAUNCHES]
        val genres = podcasts.genres().fold({ "${it.size} genres" }, { "genres: ${it.message}" })
        val best = apiCall { api.bestPodcasts() }
        best.onSuccess { response ->
            val stored = favorites.favoritePodcastIds.first()
            response.podcasts.map { it.toModel() }.firstOrNull { it.id !in stored }
                ?.let { favorites.setPodcastFavorite(it, favorite = true) }
        }
        val used = quota.quota.value?.let { "API quota: ${it.used} of ${it.freeQuota}" } ?: "API quota: unknown"
        status = best.fold(
            onSuccess = { "Launch #$launches (DataStore), $genres (cache)\n$used" },
            onFailure = { "Error: ${it.message}" },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
    ) {
        item {
            Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(Res.string.greeting, platformName()), style = MaterialTheme.typography.bodyLarge)
                Text(status, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    ThemeMode.entries.forEach { entry ->
                        FilterChip(
                            selected = entry == mode,
                            onClick = { onModeChange(entry) },
                            label = { Text(stringResource(entry.label())) },
                        )
                    }
                }
            }
        }
        // A sample low quota, to see the banner and the platform date format.
        item {
            QuotaBanner(ApiQuota(used = 2100, freeQuota = 2500, nextBillingDate = Clock.System.now() + 10.days, plan = "FREE"))
        }
        items(favoritePodcasts, key = { it.id }) { podcast ->
            PodcastListItem(
                podcast = podcast,
                isFavorite = true,
                onClick = {},
                onToggleFavorite = { scope.launch { favorites.setPodcastFavorite(podcast, favorite = false) } },
                onShare = podcast.listennotesUrl?.let { url -> { share.share(podcast.title, url, shareTitle) } },
            )
        }
        favoritePodcasts.firstOrNull()?.let { first ->
            item(key = "description") { ExpandableHtmlText(first.description, Modifier.padding(16.dp)) }
        }
    }
}

private fun ThemeMode.label() = when (this) {
    ThemeMode.SYSTEM -> Res.string.settings_theme_system
    ThemeMode.LIGHT -> Res.string.settings_theme_light
    ThemeMode.DARK -> Res.string.settings_theme_dark
}

private val LAUNCHES = intPreferencesKey("debug_launch_count")
