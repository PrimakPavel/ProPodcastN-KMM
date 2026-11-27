package com.propodcast.app.core.model

/**
 * Download state of one episode. [progress] (0..1) is known only while downloading and the server sent
 * the file size; [sizeBytes] once the file is downloaded.
 */
data class EpisodeDownloadState(
    val status: DownloadStatus,
    val progress: Float? = null,
    val sizeBytes: Long? = null,
) {
    /** Queued or downloading: can be cancelled. */
    val isActive: Boolean
        get() = status == DownloadStatus.QUEUED || status == DownloadStatus.DOWNLOADING
}
