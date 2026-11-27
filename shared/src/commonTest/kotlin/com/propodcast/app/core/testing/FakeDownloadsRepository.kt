package com.propodcast.app.core.testing

import com.propodcast.app.core.data.repository.DownloadsRepository
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.EpisodeDownload
import com.propodcast.app.core.model.EpisodeDownloadState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Records calls; tests set [downloads] directly. */
class FakeDownloadsRepository : DownloadsRepository {
    override val downloads = MutableStateFlow<List<EpisodeDownload>>(emptyList())
    override val states: Flow<Map<String, EpisodeDownloadState>> =
        downloads.map { list -> list.associate { it.episode.id to it.state } }
    val calls = mutableListOf<String>()

    override suspend fun enqueue(episode: Episode) {
        calls += "enqueue ${episode.id}"
        downloads.value = downloads.value.filterNot { it.episode.id == episode.id } +
            EpisodeDownload(episode, EpisodeDownloadState(DownloadStatus.QUEUED), addedAtMs = 0)
    }

    override suspend fun cancel(episodeId: String) {
        calls += "cancel $episodeId"
    }

    override suspend fun markRemoved(episodeId: String) {
        calls += "markRemoved $episodeId"
    }

    override suspend fun undoRemove(episodeId: String) {
        calls += "undoRemove $episodeId"
    }

    override suspend fun commitRemove(episodeId: String) {
        calls += "commitRemove $episodeId"
    }

    override suspend fun localFilePath(episodeId: String): String? = null
}
