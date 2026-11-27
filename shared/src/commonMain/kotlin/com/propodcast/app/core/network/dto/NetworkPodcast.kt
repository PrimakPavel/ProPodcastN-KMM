package com.propodcast.app.core.network.dto

import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.model.PodcastDetails
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A podcast from `best_podcasts`, `recommendations` and `podcasts/{id}`.
 * Only `podcasts/{id}` fills [episodes] and [nextEpisodePubDate].
 */
@Serializable
data class NetworkPodcast(
    val id: String,
    val title: String = "",
    val publisher: String = "",
    val image: String? = null,
    val thumbnail: String? = null,
    val description: String = "",
    val language: String? = null,
    val country: String? = null,
    val website: String? = null,
    @SerialName("listennotes_url") val listennotesUrl: String? = null,
    @SerialName("total_episodes") val totalEpisodes: Int = 0,
    @SerialName("genre_ids") val genreIds: List<Int> = emptyList(),
    @SerialName("latest_pub_date_ms") val latestPubDateMs: Long? = null,
    @SerialName("explicit_content") val explicitContent: Boolean = false,
    val episodes: List<NetworkEpisode> = emptyList(),
    @SerialName("next_episode_pub_date") val nextEpisodePubDate: Long? = null,
)

fun NetworkPodcast.toModel(): Podcast {
    val image = image?.ifBlank { null }
    val thumbnail = thumbnail?.ifBlank { null }
    return Podcast(
        id = id,
        title = title,
        publisher = publisher,
        imageUrl = image ?: thumbnail,
        thumbnailUrl = thumbnail ?: image,
        description = description,
        totalEpisodes = totalEpisodes,
        listennotesUrl = listennotesUrl?.ifBlank { null },
        latestPubDateMs = latestPubDateMs,
        language = language?.ifBlank { null },
        country = country?.ifBlank { null },
        website = website?.ifBlank { null },
        genreIds = genreIds,
        explicitContent = explicitContent,
    )
}

/** Maps `podcasts/{id}`. Episodes without their own artwork get the podcast's. */
fun NetworkPodcast.toDetails(): PodcastDetails {
    val podcast = toModel()
    return PodcastDetails(
        podcast = podcast,
        episodes = episodes.map { episode ->
            episode.toModel(
                podcastId = podcast.id,
                podcastTitle = podcast.title,
                fallbackImageUrl = podcast.imageUrl,
                fallbackThumbnailUrl = podcast.thumbnailUrl,
            )
        },
        nextEpisodePubDate = nextEpisodePubDate,
    )
}
