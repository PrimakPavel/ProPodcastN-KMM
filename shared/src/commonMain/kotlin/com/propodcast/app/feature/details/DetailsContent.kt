package com.propodcast.app.feature.details

import com.propodcast.app.core.common.AppError
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast

/** Main content of the details screen: the podcast and the episodes loaded so far. */
sealed interface DetailsContent {
    data object Loading : DetailsContent

    data class Error(val error: AppError) : DetailsContent

    data class Loaded(
        val podcast: Podcast,
        val episodes: List<Episode>,
        /** Null when all episodes are loaded. */
        val nextEpisodePubDate: Long?,
        val isLoadingMore: Boolean = false,
        val loadMoreError: AppError? = null,
    ) : DetailsContent {
        val canLoadMore: Boolean
            get() = nextEpisodePubDate != null
    }
}
