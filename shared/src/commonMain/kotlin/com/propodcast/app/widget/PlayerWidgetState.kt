package com.propodcast.app.widget

import com.propodcast.app.core.model.LastPlayed
import com.propodcast.app.core.player.PlayerState

/**
 * What the widget shows. No playback position on purpose: the widget is redrawn only when the episode,
 * play / pause or the error changes, not on every progress tick.
 */
data class PlayerWidgetState(
    val episodeTitle: String,
    val podcastTitle: String?,
    val imageUrl: String?,
    val isPlaying: Boolean,
    val hasError: Boolean = false,
)

/** The live episode, else the one saved from the last session; null when nothing was played yet. */
fun playerWidgetState(player: PlayerState, lastPlayed: LastPlayed?): PlayerWidgetState? {
    player.nowPlaying?.let { item ->
        return PlayerWidgetState(
            episodeTitle = item.title,
            podcastTitle = item.podcastTitle,
            imageUrl = item.imageUrl,
            isPlaying = player.isPlaying,
            hasError = player.error != null,
        )
    }
    return lastPlayed?.let { last ->
        PlayerWidgetState(
            episodeTitle = last.title,
            podcastTitle = last.podcastTitle,
            imageUrl = last.imageUrl,
            isPlaying = false,
        )
    }
}
