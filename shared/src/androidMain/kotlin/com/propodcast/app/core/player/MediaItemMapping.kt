package com.propodcast.app.core.player

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.propodcast.app.core.model.NowPlaying

/**
 * Plays [playUri] (the downloaded file, or the stream). The playable URI also goes into requestMetadata:
 * MediaItem.localConfiguration does not survive the trip from MediaController to MediaSession, so
 * [PlaybackService] rebuilds it from there. The stream URL is kept in the extras, so the episode is still
 * remembered by its stream URL when it plays from a file that may be deleted later.
 */
internal fun NowPlaying.toMediaItem(playUri: Uri = audioUrl.toUri()): MediaItem {
    val uri = playUri
    return MediaItem.Builder()
        .setMediaId(episodeId)
        .setUri(uri)
        .setRequestMetadata(MediaItem.RequestMetadata.Builder().setMediaUri(uri).build())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(podcastTitle)
                .setArtworkUri(imageUrl?.toUri())
                .setDurationMs(durationMs.takeIf { it > 0 })
                .setMediaType(MediaMetadata.MEDIA_TYPE_PODCAST_EPISODE)
                .setExtras(Bundle().apply { putString(EXTRA_STREAM_URL, audioUrl) })
                .build(),
        )
        .build()
}

internal fun MediaItem.toNowPlaying(): NowPlaying? {
    val uri: Uri = localConfiguration?.uri ?: requestMetadata.mediaUri ?: return null
    return NowPlaying(
        episodeId = mediaId,
        title = mediaMetadata.title?.toString().orEmpty(),
        podcastTitle = mediaMetadata.artist?.toString(),
        audioUrl = mediaMetadata.extras?.getString(EXTRA_STREAM_URL) ?: uri.toString(),
        imageUrl = mediaMetadata.artworkUri?.toString(),
        durationMs = mediaMetadata.durationMs ?: 0,
    )
}

/** True when this item plays a downloaded file rather than the stream. */
internal fun MediaItem.playsLocalFile(): Boolean =
    (localConfiguration?.uri ?: requestMetadata.mediaUri)?.scheme == ContentResolver.SCHEME_FILE

/** Restores the playable URI on the session side (see [toMediaItem]). */
internal fun MediaItem.withPlayableUri(): MediaItem =
    if (localConfiguration != null) this else buildUpon().setUri(requestMetadata.mediaUri).build()

private const val EXTRA_STREAM_URL = "stream_url"
