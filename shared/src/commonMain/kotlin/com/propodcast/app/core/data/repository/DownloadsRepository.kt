package com.propodcast.app.core.data.repository

import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.EpisodeDownload
import com.propodcast.app.core.model.EpisodeDownloadState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Episodes downloaded for offline listening. */
interface DownloadsRepository {
    /** Newest first; live progress for running downloads. */
    val downloads: Flow<List<EpisodeDownload>>

    /** episodeId → state, for the indicators in episode lists. */
    val states: Flow<Map<String, EpisodeDownloadState>>

    /** Queues the download (again, if it failed), Wi-Fi only if so set. Episodes without audio are ignored. */
    suspend fun enqueue(episode: Episode)

    /** Stops a queued or running download and forgets it. */
    suspend fun cancel(episodeId: String)

    /** Hides a download for an Undo window; [commitRemove] deletes the file, [undoRemove] brings it back. */
    suspend fun markRemoved(episodeId: String)
    suspend fun undoRemove(episodeId: String)
    suspend fun commitRemove(episodeId: String)

    /** Path of the downloaded file to play instead of streaming, if there is one. */
    suspend fun localFilePath(episodeId: String): String?
}

/** Temporary [DownloadsRepository] until the platform downloaders (stage 7): nothing is downloaded. */
class StubDownloadsRepository : DownloadsRepository {
    override val downloads: Flow<List<EpisodeDownload>> = MutableStateFlow(emptyList())
    override val states: Flow<Map<String, EpisodeDownloadState>> = MutableStateFlow(emptyMap())
    override suspend fun enqueue(episode: Episode) = Unit
    override suspend fun cancel(episodeId: String) = Unit
    override suspend fun markRemoved(episodeId: String) = Unit
    override suspend fun undoRemove(episodeId: String) = Unit
    override suspend fun commitRemove(episodeId: String) = Unit
    override suspend fun localFilePath(episodeId: String): String? = null
}
