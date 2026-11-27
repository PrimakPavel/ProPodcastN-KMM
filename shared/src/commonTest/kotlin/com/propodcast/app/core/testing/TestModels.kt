package com.propodcast.app.core.testing

import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast

fun testPodcast(id: String, title: String = "Podcast $id") = Podcast(
    id = id,
    title = title,
    publisher = "Publisher",
    imageUrl = null,
    thumbnailUrl = null,
    description = "",
    totalEpisodes = 10,
    listennotesUrl = "https://www.listennotes.com/c/$id/",
)

fun testEpisode(id: String, podcastId: String = "p1") = Episode(
    id = id,
    title = "Episode $id",
    description = "",
    audioUrl = "https://example.com/$id.mp3",
    imageUrl = null,
    thumbnailUrl = null,
    pubDateMs = 1_000,
    durationSec = 60,
    listennotesUrl = "https://www.listennotes.com/e/$id/",
    podcastId = podcastId,
    podcastTitle = "Podcast $podcastId",
)
