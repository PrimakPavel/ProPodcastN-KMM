package com.propodcast.app.core.model

/** A playable episode. [podcastId] / [podcastTitle] identify its podcast (used by favorites and the player). */
data class Episode(
    val id: String,
    val title: String,
    val description: String,
    val audioUrl: String?,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val pubDateMs: Long,
    val durationSec: Int,
    val listennotesUrl: String?,
    val podcastId: String?,
    val podcastTitle: String?,
    val explicitContent: Boolean = false,
)
