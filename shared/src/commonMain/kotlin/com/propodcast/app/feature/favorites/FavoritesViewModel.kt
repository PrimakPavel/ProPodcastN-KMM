package com.propodcast.app.feature.favorites

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.action_undo
import com.propodcast.app.resources.favorite_removed
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propodcast.app.core.data.repository.DownloadsRepository
import com.propodcast.app.core.data.repository.FavoritesRepository
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.model.RemovedFavorite
import com.propodcast.app.core.ui.SnackbarEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Favorites and downloads from Room; removing a favorite offers Undo through [snackbarEvents]. */
class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository,
    downloadsRepository: DownloadsRepository,
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = combine(
        favoritesRepository.favoritePodcasts,
        favoritesRepository.favoriteEpisodes,
        downloadsRepository.downloads,
    ) { podcasts, episodes, downloads ->
        FavoritesUiState(
            podcasts = podcasts,
            episodes = episodes,
            // Running downloads on top; the order within each group (newest first) stays.
            downloads = downloads.sortedByDescending { it.state.isActive },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), FavoritesUiState())

    private val snackbarChannel = Channel<SnackbarEvent>(Channel.BUFFERED)

    /** "Removed “…” · Undo" after each removal, shown by SnackbarEffect. */
    val snackbarEvents: Flow<SnackbarEvent> = snackbarChannel.receiveAsFlow()

    fun removePodcast(podcast: Podcast) {
        viewModelScope.launch { favoritesRepository.removePodcast(podcast.id)?.let { offerUndo(it) } }
    }

    fun removeEpisode(episode: Episode) {
        viewModelScope.launch { favoritesRepository.removeEpisode(episode.id)?.let { offerUndo(it) } }
    }

    fun undoRemove(removed: RemovedFavorite) {
        viewModelScope.launch { favoritesRepository.restore(removed) }
    }

    private suspend fun offerUndo(removed: RemovedFavorite) {
        val title = when (removed) {
            is RemovedFavorite.PodcastItem -> removed.podcast.title
            is RemovedFavorite.EpisodeItem -> removed.episode.title
        }
        snackbarChannel.send(
            SnackbarEvent(
                messageRes = Res.string.favorite_removed,
                messageArgs = listOf(title),
                actionLabelRes = Res.string.action_undo,
                onAction = { undoRemove(removed) },
            ),
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
