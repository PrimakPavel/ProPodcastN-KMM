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
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.propodcast.app.core.analytics.NoOpAnalyticsTracker
import com.propodcast.app.core.common.apiCall
import com.propodcast.app.core.data.AppStorage
import com.propodcast.app.core.data.cache.DataStoreReferenceDataCache
import com.propodcast.app.core.data.repository.DefaultFavoritesRepository
import com.propodcast.app.core.data.repository.DefaultPodcastRepository
import com.propodcast.app.core.network.dto.toModel
import com.propodcast.app.core.network.listenNotesJson
import kotlinx.coroutines.flow.first
import com.propodcast.app.core.network.ApiConfig
import com.propodcast.app.core.network.ListenNotesApi
import com.propodcast.app.core.network.createListenNotesHttpClient
import com.propodcast.app.resources.Res
import com.propodcast.app.resources.app_name
import com.propodcast.app.resources.greeting
import org.jetbrains.compose.resources.stringResource

@Composable
fun App(storage: AppStorage) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
        Surface(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.safeDrawingPadding().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(Res.string.greeting, platformName()), style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(24.dp))
                StorageCheck(storage)
            }
        }
    }
}

/**
 * Temporary check of the network and data stages: counts launches in DataStore, loads best podcasts
 * through the repository (genres via the on-disk cache) and adds the next one to favorites in Room.
 * Both numbers grow across restarts when storage works.
 */
@Composable
private fun StorageCheck(storage: AppStorage) {
    var text by remember { mutableStateOf("Loading…") }
    LaunchedEffect(Unit) {
        val launches = storage.userPreferences.edit { prefs ->
            prefs[LAUNCHES] = (prefs[LAUNCHES] ?: 0) + 1
        }[LAUNCHES]
        val config = ApiConfig.fromBuildKonfig()
        val client = createListenNotesHttpClient(config, logging = BuildKonfig.HTTP_LOGGING)
        val api = ListenNotesApi(client)
        val podcasts = DefaultPodcastRepository(
            api = api,
            cache = DataStoreReferenceDataCache(storage.referenceCache, listenNotesJson, config),
            analytics = NoOpAnalyticsTracker,
        )
        val favorites = DefaultFavoritesRepository(
            podcastDao = storage.database.favoritePodcastDao(),
            episodeDao = storage.database.favoriteEpisodeDao(),
            analytics = NoOpAnalyticsTracker,
        )
        val genres = podcasts.genres().fold({ "${it.size} genres" }, { "genres: ${it.message}" })
        text = apiCall { api.bestPodcasts() }.fold(
            onSuccess = { response ->
                val stored = favorites.favoritePodcastIds.first()
                response.podcasts.map { it.toModel() }.firstOrNull { it.id !in stored }
                    ?.let { favorites.setPodcastFavorite(it, favorite = true) }
                val favoriteTitles = favorites.favoritePodcasts.first().map { it.title }
                "Launch #$launches (DataStore), $genres (cache)\n\n" +
                    "Favorites in Room (${favoriteTitles.size}):\n" + favoriteTitles.take(5).joinToString("\n")
            },
            onFailure = { "Error: ${it.message}" },
        )
        client.close()
    }
    Text(text, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
}

private val LAUNCHES = intPreferencesKey("debug_launch_count")
