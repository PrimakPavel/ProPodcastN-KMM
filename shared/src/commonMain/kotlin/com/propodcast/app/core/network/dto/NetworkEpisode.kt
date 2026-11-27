package com.propodcast.app.core.network.dto

import com.propodcast.app.core.model.Episode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkEpisode(
    val id: String,
    val title: String = "",
    val description: String = "",
    val audio: String? = null,
    val image: String? = null,
    val thumbnail: String? = null,
    @SerialName("pub_date_ms") val pubDateMs: Long = 0,
    @SerialName("audio_length_sec") val audioLengthSec: Int = 0,
    @SerialName("listennotes_url") val listennotesUrl: String? = null,
    @SerialName("explicit_content") val explicitContent: Boolean = false,
)

/**
 * Artwork falls back from image ↔ thumbnail, then to the podcast's
 * [fallbackImageUrl] / [fallbackThumbnailUrl].
 */
fun NetworkEpisode.toModel(
    podcastId: String?,
    podcastTitle: String?,
    fallbackImageUrl: String? = null,
    fallbackThumbnailUrl: String? = null,
): Episode {
    val image = image?.ifBlank { null }
    val thumbnail = thumbnail?.ifBlank { null }
    return Episode(
        id = id,
        title = title,
        description = description,
        audioUrl = audio?.ifBlank { null },
        imageUrl = image ?: thumbnail ?: fallbackImageUrl ?: fallbackThumbnailUrl,
        thumbnailUrl = thumbnail ?: image ?: fallbackThumbnailUrl ?: fallbackImageUrl,
        pubDateMs = pubDateMs,
        durationSec = audioLengthSec,
        listennotesUrl = listennotesUrl?.ifBlank { null },
        podcastId = podcastId,
        podcastTitle = podcastTitle,
        explicitContent = explicitContent,
    )
}
