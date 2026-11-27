package com.propodcast.app.core.model

/** The last episode the user played, used to resume playback and by the widget. */
data class LastPlayed(
    val episodeId: String,
    val title: String,
    val podcastTitle: String?,
    val audioUrl: String,
    val imageUrl: String?,
    val positionMs: Long,
    val durationMs: Long = 0,
)
