package com.propodcast.app.feature.details

import com.propodcast.app.core.model.Podcast

data class PodcastDetailsUiState(
    val content: DetailsContent = DetailsContent.Loading,
    /** Empty while loading or if recommendations could not be loaded (the section is then hidden). */
    val recommendations: List<Podcast> = emptyList(),
    val isPodcastFavorite: Boolean = false,
    val favoriteEpisodeIds: Set<String> = emptySet(),
)
