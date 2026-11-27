package com.propodcast.app.core.model

/** A favorite that was just removed, kept so Undo can put it back at its old position. */
sealed interface RemovedFavorite {
    val addedAtMs: Long

    data class PodcastItem(val podcast: Podcast, override val addedAtMs: Long) : RemovedFavorite

    data class EpisodeItem(val episode: Episode, override val addedAtMs: Long) : RemovedFavorite
}
