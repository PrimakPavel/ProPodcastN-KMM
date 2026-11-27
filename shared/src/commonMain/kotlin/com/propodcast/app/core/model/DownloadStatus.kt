package com.propodcast.app.core.model

/** Lifecycle of an episode download. Stored by name in Room. */
enum class DownloadStatus {
    /** Waiting to start (also while waiting for a network or between retries). */
    QUEUED,
    DOWNLOADING,
    DOWNLOADED,
    FAILED,
}
