package com.propodcast.app.feature.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propodcast.app.core.common.toAppError
import com.propodcast.app.core.data.repository.FavoritesRepository
import com.propodcast.app.core.data.repository.PodcastRepository
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.navigation.PodcastDetailsRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Podcast details with episodes loaded page by page ("Load more", keyed by `next_episode_pub_date`)
 * and recommendations. Loading more is an explicit action to save the API quota.
 */
class PodcastDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val podcastRepository: PodcastRepository,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    /** Type-safe route args are stored under their property names. */
    private val podcastId: String = checkNotNull(savedStateHandle[PodcastDetailsRoute.ARG_PODCAST_ID]) {
        "PodcastDetailsRoute.podcastId is missing"
    }

    private val content = MutableStateFlow<DetailsContent>(DetailsContent.Loading)
    private val recommendations = MutableStateFlow(emptyList<Podcast>())

    val uiState: StateFlow<PodcastDetailsUiState> = combine(
        content,
        recommendations,
        favoritesRepository.favoritePodcastIds,
        favoritesRepository.favoriteEpisodeIds,
    ) { content, recommendations, favoritePodcastIds, favoriteEpisodeIds ->
        PodcastDetailsUiState(
            content = content,
            recommendations = recommendations,
            isPodcastFavorite = podcastId in favoritePodcastIds,
            favoriteEpisodeIds = favoriteEpisodeIds,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PodcastDetailsUiState())

    init {
        load()
        loadRecommendations()
    }

    /** Loads (or reloads after an error) the podcast and its first page of episodes. */
    fun load() {
        content.value = DetailsContent.Loading
        viewModelScope.launch {
            content.value = podcastRepository.podcastDetails(podcastId).fold(
                onSuccess = { details ->
                    val episodes = details.episodes.distinctBy { it.id }
                    DetailsContent.Loaded(
                        podcast = details.podcast,
                        episodes = episodes,
                        nextEpisodePubDate = details.nextEpisodePubDate.takeIf { episodes.isNotEmpty() },
                    )
                },
                onFailure = { DetailsContent.Error(it.toAppError()) },
            )
        }
    }

    fun loadMoreEpisodes() {
        val current = content.value as? DetailsContent.Loaded ?: return
        val next = current.nextEpisodePubDate ?: return
        if (current.isLoadingMore) return
        content.value = current.copy(isLoadingMore = true, loadMoreError = null)
        viewModelScope.launch {
            podcastRepository.podcastDetails(podcastId, nextEpisodePubDate = next).fold(
                onSuccess = { page -> appendEpisodes(page.episodes, page.nextEpisodePubDate) },
                onFailure = { error ->
                    content.update { state ->
                        (state as? DetailsContent.Loaded)
                            ?.copy(isLoadingMore = false, loadMoreError = error.toAppError())
                            ?: state
                    }
                },
            )
        }
    }

    fun togglePodcastFavorite() {
        val loaded = content.value as? DetailsContent.Loaded ?: return
        val makeFavorite = !uiState.value.isPodcastFavorite
        viewModelScope.launch { favoritesRepository.setPodcastFavorite(loaded.podcast, makeFavorite) }
    }

    fun toggleEpisodeFavorite(episode: Episode) {
        val makeFavorite = episode.id !in uiState.value.favoriteEpisodeIds
        viewModelScope.launch { favoritesRepository.setEpisodeFavorite(episode, makeFavorite) }
    }

    /**
     * Adds new episodes. If the page brings nothing new (the mock server repeats the same page),
     * loading more stops instead of offering an endless "Load more".
     */
    private fun appendEpisodes(page: List<Episode>, nextEpisodePubDate: Long?) {
        content.update { state ->
            val loaded = state as? DetailsContent.Loaded ?: return@update state
            val known = loaded.episodes.mapTo(HashSet()) { it.id }
            val newEpisodes = page.filter { known.add(it.id) }
            loaded.copy(
                episodes = loaded.episodes + newEpisodes,
                nextEpisodePubDate = nextEpisodePubDate.takeIf { newEpisodes.isNotEmpty() },
                isLoadingMore = false,
                loadMoreError = null,
            )
        }
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            // Recommendations are optional: on failure the section is simply not shown.
            recommendations.value = podcastRepository.recommendations(podcastId)
                .getOrDefault(emptyList())
                .filterNot { it.id == podcastId }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
