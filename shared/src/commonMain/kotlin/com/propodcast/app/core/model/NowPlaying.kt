package com.propodcast.app.core.model

/** The episode loaded in the player. [durationMs] is 0 when unknown. */
data class NowPlaying(
    val episodeId: String,
    val title: String,
    val podcastTitle: String?,
    val audioUrl: String,
    val imageUrl: String?,
    val durationMs: Long = 0,
)

/** Null when the episode has no audio file to play. */
fun Episode.toNowPlaying(): NowPlaying? = audioUrl?.let { url ->
    NowPlaying(
        episodeId = id,
        title = title,
        podcastTitle = podcastTitle,
        audioUrl = url,
        imageUrl = imageUrl ?: thumbnailUrl,
        durationMs = durationSec * 1_000L,
    )
}

fun NowPlaying.toLastPlayed(positionMs: Long) = LastPlayed(
    episodeId = episodeId,
    title = title,
    podcastTitle = podcastTitle,
    audioUrl = audioUrl,
    imageUrl = imageUrl,
    positionMs = positionMs,
    durationMs = durationMs,
)

fun LastPlayed.toNowPlaying() = NowPlaying(
    episodeId = episodeId,
    title = title,
    podcastTitle = podcastTitle,
    audioUrl = audioUrl,
    imageUrl = imageUrl,
    durationMs = durationMs,
)

/** The player's episode as an [Episode] (e.g. to download it); what the player doesn't know stays empty. */
fun NowPlaying.toEpisode() = Episode(
    id = episodeId,
    title = title,
    description = "",
    audioUrl = audioUrl,
    imageUrl = imageUrl,
    thumbnailUrl = imageUrl,
    pubDateMs = 0,
    durationSec = (durationMs / 1_000).toInt(),
    listennotesUrl = null,
    podcastId = null,
    podcastTitle = podcastTitle,
)
