package com.propodcast.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.EpisodeDownload
import com.propodcast.app.core.model.EpisodeDownloadState

/**
 * A downloaded (or downloading) episode with enough metadata to list and play it offline.
 * [pendingDelete] hides a removed download while its Undo snackbar is shown; the file goes afterwards.
 */
@Entity(tableName = "downloaded_episodes")
data class DownloadedEpisodeEntity(
    @PrimaryKey @ColumnInfo(name = "episode_id") val episodeId: String,
    val title: String,
    val description: String,
    @ColumnInfo(name = "audio_url") val audioUrl: String,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String?,
    @ColumnInfo(name = "pub_date_ms") val pubDateMs: Long,
    @ColumnInfo(name = "duration_sec") val durationSec: Int,
    @ColumnInfo(name = "listennotes_url") val listennotesUrl: String?,
    @ColumnInfo(name = "podcast_id") val podcastId: String?,
    @ColumnInfo(name = "podcast_title") val podcastTitle: String?,
    val status: DownloadStatus,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long?,
    @ColumnInfo(name = "added_at") val addedAt: Long,
    @ColumnInfo(name = "pending_delete", defaultValue = "0") val pendingDelete: Boolean = false,
)

fun DownloadedEpisodeEntity.toEpisode() = Episode(
    id = episodeId,
    title = title,
    description = description,
    audioUrl = audioUrl,
    imageUrl = imageUrl,
    thumbnailUrl = thumbnailUrl,
    pubDateMs = pubDateMs,
    durationSec = durationSec,
    listennotesUrl = listennotesUrl,
    podcastId = podcastId,
    podcastTitle = podcastTitle,
)

/** [progress] comes from the running download work, not from the database. */
fun DownloadedEpisodeEntity.toDownload(progress: Float?) = EpisodeDownload(
    episode = toEpisode(),
    state = EpisodeDownloadState(
        status = status,
        progress = progress.takeIf { status == DownloadStatus.DOWNLOADING },
        sizeBytes = sizeBytes,
    ),
    addedAtMs = addedAt,
)

/** Null when the episode has no audio to download. */
fun Episode.toDownloadEntity(addedAt: Long): DownloadedEpisodeEntity? = audioUrl?.let { url ->
    DownloadedEpisodeEntity(
        episodeId = id,
        title = title,
        description = description,
        audioUrl = url,
        imageUrl = imageUrl,
        thumbnailUrl = thumbnailUrl,
        pubDateMs = pubDateMs,
        durationSec = durationSec,
        listennotesUrl = listennotesUrl,
        podcastId = podcastId,
        podcastTitle = podcastTitle,
        status = DownloadStatus.QUEUED,
        sizeBytes = null,
        addedAt = addedAt,
    )
}
