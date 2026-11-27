package com.propodcast.app.feature.favorites

import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.EpisodeDownload
import com.propodcast.app.core.model.Podcast

/**
 * Favorite podcasts and episodes, newest first, and downloads (running first). Lists stay empty while
 * [isLoading].
 */
data class FavoritesUiState(
    val podcasts: List<Podcast> = emptyList(),
    val episodes: List<Episode> = emptyList(),
    val downloads: List<EpisodeDownload> = emptyList(),
    val isLoading: Boolean = true,
)
