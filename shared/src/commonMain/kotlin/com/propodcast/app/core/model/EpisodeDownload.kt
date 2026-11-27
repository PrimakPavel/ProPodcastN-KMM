package com.propodcast.app.core.model

/** An episode in the downloads list. */
data class EpisodeDownload(
    val episode: Episode,
    val state: EpisodeDownloadState,
    val addedAtMs: Long,
)
