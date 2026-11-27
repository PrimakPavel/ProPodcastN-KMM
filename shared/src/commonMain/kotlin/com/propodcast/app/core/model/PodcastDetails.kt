package com.propodcast.app.core.model

/** One page of podcast details. Load more with [nextEpisodePubDate] while [hasMoreEpisodes]. */
data class PodcastDetails(
    val podcast: Podcast,
    val episodes: List<Episode>,
    val nextEpisodePubDate: Long?,
) {
    val hasMoreEpisodes: Boolean
        get() = nextEpisodePubDate != null && episodes.isNotEmpty()
}
