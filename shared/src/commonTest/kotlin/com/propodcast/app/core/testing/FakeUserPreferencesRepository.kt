package com.propodcast.app.core.testing

import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.model.LastPlayed
import com.propodcast.app.core.model.PodcastFilters
import com.propodcast.app.core.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPreferencesRepository(initial: PodcastFilters = PodcastFilters()) : UserPreferencesRepository {
    override val filters: MutableStateFlow<PodcastFilters> = MutableStateFlow(initial)
    override val lastPlayed: MutableStateFlow<LastPlayed?> = MutableStateFlow(null)
    override val downloadOnlyOnWifi = MutableStateFlow(false)

    override suspend fun setDownloadOnlyOnWifi(enabled: Boolean) {
        downloadOnlyOnWifi.value = enabled
    }

    override val themeMode = MutableStateFlow(ThemeMode.SYSTEM)

    override suspend fun setThemeMode(mode: ThemeMode) {
        themeMode.value = mode
    }

    override suspend fun setGenre(genreId: Int?) {
        filters.value = filters.value.copy(genreId = genreId)
    }

    override suspend fun setRegion(regionCode: String?) {
        filters.value = filters.value.copy(regionCode = regionCode)
    }

    override suspend fun setLanguage(language: String?) {
        filters.value = filters.value.copy(language = language)
    }

    override suspend fun setLastPlayed(lastPlayed: LastPlayed) {
        this.lastPlayed.value = lastPlayed
    }

    override suspend fun updateLastPlayedPosition(positionMs: Long) {
        lastPlayed.value = lastPlayed.value?.copy(positionMs = positionMs)
    }

    override suspend fun clearLastPlayed() {
        lastPlayed.value = null
    }
}
