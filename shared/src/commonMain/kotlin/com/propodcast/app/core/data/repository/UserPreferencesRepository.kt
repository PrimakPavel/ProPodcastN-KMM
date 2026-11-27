package com.propodcast.app.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.propodcast.app.core.model.LastPlayed
import com.propodcast.app.core.model.PodcastFilters
import com.propodcast.app.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Small persisted settings: list filters and the last played episode. */
interface UserPreferencesRepository {
    val filters: Flow<PodcastFilters>
    val lastPlayed: Flow<LastPlayed?>

    /** Null clears the filter ("any"). */
    suspend fun setGenre(genreId: Int?)
    suspend fun setRegion(regionCode: String?)
    suspend fun setLanguage(language: String?)

    suspend fun setLastPlayed(lastPlayed: LastPlayed)
    suspend fun updateLastPlayedPosition(positionMs: Long)
    suspend fun clearLastPlayed()

    /** Settings: downloads wait for Wi-Fi instead of using mobile data. Off by default. */
    val downloadOnlyOnWifi: Flow<Boolean>
    suspend fun setDownloadOnlyOnWifi(enabled: Boolean)

    /** Settings: app theme. Follows the device by default. */
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}

class DataStoreUserPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    override val filters: Flow<PodcastFilters> = dataStore.data
        .map { prefs ->
            PodcastFilters(
                genreId = prefs[Keys.GENRE_ID],
                regionCode = prefs[Keys.REGION],
                language = prefs[Keys.LANGUAGE],
            )
        }
        .distinctUntilChanged()

    override val lastPlayed: Flow<LastPlayed?> = dataStore.data
        .map { prefs ->
            val id = prefs[Keys.LAST_EPISODE_ID] ?: return@map null
            val audioUrl = prefs[Keys.LAST_AUDIO_URL] ?: return@map null
            LastPlayed(
                episodeId = id,
                title = prefs[Keys.LAST_TITLE].orEmpty(),
                podcastTitle = prefs[Keys.LAST_PODCAST_TITLE],
                audioUrl = audioUrl,
                imageUrl = prefs[Keys.LAST_IMAGE_URL],
                positionMs = prefs[Keys.LAST_POSITION_MS] ?: 0L,
                durationMs = prefs[Keys.LAST_DURATION_MS] ?: 0L,
            )
        }
        .distinctUntilChanged()

    override val downloadOnlyOnWifi: Flow<Boolean> = dataStore.data
        .map { it[Keys.DOWNLOAD_ONLY_ON_WIFI] ?: false }
        .distinctUntilChanged()

    override suspend fun setDownloadOnlyOnWifi(enabled: Boolean) = edit(Keys.DOWNLOAD_ONLY_ON_WIFI, enabled)

    override val themeMode: Flow<ThemeMode> = dataStore.data
        .map { prefs -> ThemeMode.entries.firstOrNull { it.name == prefs[Keys.THEME_MODE] } ?: ThemeMode.SYSTEM }
        .distinctUntilChanged()

    override suspend fun setThemeMode(mode: ThemeMode) = edit(Keys.THEME_MODE, mode.name)

    override suspend fun setGenre(genreId: Int?) = edit(Keys.GENRE_ID, genreId)

    override suspend fun setRegion(regionCode: String?) = edit(Keys.REGION, regionCode)

    override suspend fun setLanguage(language: String?) = edit(Keys.LANGUAGE, language)

    override suspend fun setLastPlayed(lastPlayed: LastPlayed) {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_EPISODE_ID] = lastPlayed.episodeId
            prefs[Keys.LAST_TITLE] = lastPlayed.title
            prefs[Keys.LAST_AUDIO_URL] = lastPlayed.audioUrl
            prefs[Keys.LAST_POSITION_MS] = lastPlayed.positionMs
            prefs[Keys.LAST_DURATION_MS] = lastPlayed.durationMs
            prefs.setOrRemove(Keys.LAST_PODCAST_TITLE, lastPlayed.podcastTitle)
            prefs.setOrRemove(Keys.LAST_IMAGE_URL, lastPlayed.imageUrl)
        }
    }

    override suspend fun updateLastPlayedPosition(positionMs: Long) {
        dataStore.edit { prefs ->
            if (prefs[Keys.LAST_EPISODE_ID] != null) prefs[Keys.LAST_POSITION_MS] = positionMs
        }
    }

    override suspend fun clearLastPlayed() {
        dataStore.edit { prefs ->
            listOf(
                Keys.LAST_EPISODE_ID, Keys.LAST_TITLE, Keys.LAST_PODCAST_TITLE,
                Keys.LAST_AUDIO_URL, Keys.LAST_IMAGE_URL, Keys.LAST_POSITION_MS, Keys.LAST_DURATION_MS,
            ).forEach { prefs.remove(it) }
        }
    }

    private suspend fun <T> edit(key: Preferences.Key<T>, value: T?) {
        dataStore.edit { it.setOrRemove(key, value) }
    }

    private fun <T> androidx.datastore.preferences.core.MutablePreferences.setOrRemove(key: Preferences.Key<T>, value: T?) {
        if (value == null) remove(key) else set(key, value)
    }

    private object Keys {
        val GENRE_ID = intPreferencesKey("filter_genre_id")
        val REGION = stringPreferencesKey("filter_region")
        val LANGUAGE = stringPreferencesKey("filter_language")

        val LAST_EPISODE_ID = stringPreferencesKey("last_episode_id")
        val LAST_TITLE = stringPreferencesKey("last_title")
        val LAST_PODCAST_TITLE = stringPreferencesKey("last_podcast_title")
        val LAST_AUDIO_URL = stringPreferencesKey("last_audio_url")
        val LAST_IMAGE_URL = stringPreferencesKey("last_image_url")
        val LAST_POSITION_MS = longPreferencesKey("last_position_ms")
        val LAST_DURATION_MS = longPreferencesKey("last_duration_ms")

        val DOWNLOAD_ONLY_ON_WIFI = booleanPreferencesKey("download_only_on_wifi")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
