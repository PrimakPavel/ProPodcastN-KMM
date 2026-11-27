package com.propodcast.app.core.model

/** A podcast as shown in lists, details and favorites. */
data class Podcast(
    val id: String,
    val title: String,
    val publisher: String,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val description: String,
    val totalEpisodes: Int,
    val listennotesUrl: String?,
    val latestPubDateMs: Long? = null,
    val language: String? = null,
    val country: String? = null,
    val website: String? = null,
    val genreIds: List<Int> = emptyList(),
    val explicitContent: Boolean = false,
)
