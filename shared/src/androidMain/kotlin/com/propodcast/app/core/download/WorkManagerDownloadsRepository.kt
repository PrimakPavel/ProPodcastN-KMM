package com.propodcast.app.core.download

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.propodcast.app.core.analytics.AnalyticsEvents
import com.propodcast.app.core.analytics.AnalyticsTracker
import com.propodcast.app.core.data.repository.DownloadsRepository
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.database.dao.DownloadedEpisodeDao
import com.propodcast.app.core.database.entity.toDownload
import com.propodcast.app.core.database.entity.toDownloadEntity
import com.propodcast.app.core.download.EpisodeDownloadWorker
import com.propodcast.app.core.download.EpisodeFileStore
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.EpisodeDownload
import com.propodcast.app.core.model.EpisodeDownloadState
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** [DownloadsRepository] on WorkManager: downloads survive app restarts and can wait for Wi-Fi. */
class WorkManagerDownloadsRepository(
    private val dao: DownloadedEpisodeDao,
    private val workManager: WorkManager,
    private val files: EpisodeFileStore,
    private val analytics: AnalyticsTracker,
    private val preferences: UserPreferencesRepository,
    private val clock: Clock = Clock.System,
) : DownloadsRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Removals whose Undo window was cut short by the process dying.
        scope.launch {
            dao.pendingDeleteIds().forEach { deleteNow(it) }
        }
    }

    /** Progress of running downloads, by episode. */
    private val progress: Flow<Map<String, Float>> =
        workManager.getWorkInfosByTagFlow(EpisodeDownloadWorker.TAG).map { infos ->
            infos.filter { it.state == WorkInfo.State.RUNNING }
                .mapNotNull { info ->
                    val episodeId = EpisodeDownloadWorker.episodeIdFromTags(info.tags) ?: return@mapNotNull null
                    val value = info.progress.getFloat(EpisodeDownloadWorker.KEY_PROGRESS, -1f)
                    episodeId to value
                }
                .filter { (_, value) -> value >= 0f }
                .toMap()
        }

    override val downloads: Flow<List<EpisodeDownload>> =
        combine(dao.observeVisible(), progress) { rows, running ->
            rows.map { it.toDownload(running[it.episodeId]) }
        }.distinctUntilChanged()

    override val states: Flow<Map<String, EpisodeDownloadState>> =
        downloads.map { list -> list.associate { it.episode.id to it.state } }.distinctUntilChanged()

    override suspend fun enqueue(episode: Episode) {
        val entity = episode.toDownloadEntity(addedAt = clock.now().toEpochMilliseconds()) ?: return
        dao.upsert(entity)
        workManager.enqueueUniqueWork(
            EpisodeDownloadWorker.uniqueWorkName(episode.id),
            ExistingWorkPolicy.REPLACE,
            EpisodeDownloadWorker.request(episode.id, wifiOnly = preferences.downloadOnlyOnWifi.first()),
        )
        analytics.logEvent(AnalyticsEvents.DOWNLOAD_EPISODE)
    }

    override suspend fun cancel(episodeId: String) {
        workManager.cancelUniqueWork(EpisodeDownloadWorker.uniqueWorkName(episodeId))
        deleteNow(episodeId)
    }

    override suspend fun markRemoved(episodeId: String) = dao.setPendingDelete(episodeId, true)

    override suspend fun undoRemove(episodeId: String) = dao.setPendingDelete(episodeId, false)

    override suspend fun commitRemove(episodeId: String) {
        if (dao.get(episodeId)?.pendingDelete == true) deleteNow(episodeId)
    }

    override suspend fun localFilePath(episodeId: String): String? {
        val row = dao.get(episodeId) ?: return null
        if (row.status != DownloadStatus.DOWNLOADED || row.pendingDelete) return null
        return files.file(episodeId).takeIf { it.exists() }?.absolutePath
    }

    private suspend fun deleteNow(episodeId: String) {
        files.delete(episodeId)
        dao.delete(episodeId)
    }
}
