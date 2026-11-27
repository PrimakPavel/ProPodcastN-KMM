package com.propodcast.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.propodcast.app.core.data.repository.ApiQuotaRepository
import com.propodcast.app.core.data.repository.DownloadsRepository
import com.propodcast.app.core.data.repository.FavoritesRepository
import com.propodcast.app.core.data.repository.PodcastRepository
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.model.ApiQuota
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.model.Podcast
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Best podcasts for the saved genre / region filters. */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    podcastRepository: PodcastRepository,
    userPreferences: UserPreferencesRepository,
    apiQuotaRepository: ApiQuotaRepository,
    private val favoritesRepository: FavoritesRepository,
    downloadsRepository: DownloadsRepository,
) : ViewModel() {

    /** Restarts paging from page 1 whenever the genre or region filter changes. */
    val podcasts: Flow<PagingData<Podcast>> = userPreferences.filters
        .map { it.genreId to it.regionCode }
        .distinctUntilChanged()
        .flatMapLatest { (genreId, region) -> podcastRepository.bestPodcasts(genreId, region) }
        .cachedIn(viewModelScope)

    val favoriteIds: StateFlow<Set<String>> = favoritesRepository.favoritePodcastIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptySet())

    val quota: StateFlow<ApiQuota?> = apiQuotaRepository.quota

    /** At least one episode can be played offline: worth offering when the list can't load. */
    val hasDownloads: StateFlow<Boolean> = downloadsRepository.downloads
        .map { list -> list.any { it.state.status == DownloadStatus.DOWNLOADED } }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    fun toggleFavorite(podcast: Podcast) {
        val makeFavorite = podcast.id !in favoriteIds.value
        viewModelScope.launch { favoritesRepository.setPodcastFavorite(podcast, makeFavorite) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
