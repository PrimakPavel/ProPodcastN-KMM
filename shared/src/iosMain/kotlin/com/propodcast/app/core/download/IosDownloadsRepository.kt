package com.propodcast.app.core.download

import co.touchlab.kermit.Logger
import com.propodcast.app.core.analytics.AnalyticsEvents
import com.propodcast.app.core.analytics.AnalyticsTracker
import com.propodcast.app.core.common.persistentNonBackedUpDirectory
import com.propodcast.app.core.data.repository.DownloadsRepository
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.database.dao.DownloadedEpisodeDao
import com.propodcast.app.core.database.entity.toDownload
import com.propodcast.app.core.database.entity.toDownloadEntity
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.EpisodeDownload
import com.propodcast.app.core.model.EpisodeDownloadState
import kotlin.time.Clock
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSNumber
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.Foundation.NSURLErrorCancelled
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.NSURLSessionDownloadDelegateProtocol
import platform.Foundation.NSURLSessionDownloadTask
import platform.Foundation.NSURLSessionTask
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * [DownloadsRepository] on a background `NSURLSession`: iOS keeps downloading while the app is suspended or
 * closed and wakes the app to deliver the files (see [handleBackgroundEvents]). Each task carries its episode id
 * in `taskDescription`. "Wi-Fi only" sets the request's cellular / expensive network access to false, so the
 * system waits for Wi-Fi, like WorkManager's unmetered constraint.
 */
@OptIn(ExperimentalForeignApi::class)
class IosDownloadsRepository(
    private val dao: DownloadedEpisodeDao,
    private val analytics: AnalyticsTracker,
    private val preferences: UserPreferencesRepository,
    private val progressPresenter: DownloadProgressPresenter? = null,
    private val clock: Clock = Clock.System,
) : DownloadsRepository {

    private val log = Logger.withTag("IosDownloadsRepository")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val files = IosEpisodeFileStore()

    /** Progress of running downloads, by episode. */
    private val progress = MutableStateFlow<Map<String, Float?>>(emptyMap())

    private val delegate = Delegate()
    private val session: NSURLSession = NSURLSession.sessionWithConfiguration(
        NSURLSessionConfiguration.backgroundSessionConfigurationWithIdentifier(SESSION_ID).apply {
            sessionSendsLaunchEvents = true
            discretionary = false
        },
        delegate = delegate,
        delegateQueue = NSOperationQueue.mainQueue,
    )

    init {
        progressPresenter?.let { presenter ->
            scope.presentDownloadProgress(progress, presenter) { id -> dao.get(id)?.title }
        }
        scope.launch {
            // Removals whose Undo window was cut short by the app being closed.
            dao.pendingDeleteIds().forEach { deleteNow(it) }
            resumeLostDownloads()
        }
    }

    override val downloads: Flow<List<EpisodeDownload>> =
        combine(dao.observeVisible(), progress) { rows, running ->
            rows.map { it.toDownload(running[it.episodeId]) }
        }.distinctUntilChanged()

    override val states: Flow<Map<String, EpisodeDownloadState>> =
        downloads.map { list -> list.associate { it.episode.id to it.state } }.distinctUntilChanged()

    override suspend fun enqueue(episode: Episode) {
        val entity = episode.toDownloadEntity(addedAt = clock.now().toEpochMilliseconds()) ?: return
        cancelTask(episode.id)
        dao.upsert(entity)
        // Listed as running right away (size unknown), so its Live Activity starts while the app is in front.
        progress.update { it + (episode.id to null) }
        start(episode.id, entity.audioUrl, wifiOnly = preferences.downloadOnlyOnWifi.first())
        analytics.logEvent(AnalyticsEvents.DOWNLOAD_EPISODE)
    }

    override suspend fun cancel(episodeId: String) {
        cancelTask(episodeId)
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
        return files.path(episodeId).takeIf { files.exists(it) }
    }

    private fun start(episodeId: String, audioUrl: String, wifiOnly: Boolean) {
        val url = NSURL.URLWithString(audioUrl) ?: return
        val request = NSMutableURLRequest(uRL = url).apply {
            setAllowsCellularAccess(!wifiOnly)
            setAllowsExpensiveNetworkAccess(!wifiOnly)
        }
        session.downloadTaskWithRequest(request).apply {
            taskDescription = episodeId
            resume()
        }
    }

    /** Rows still queued or downloading without a task (e.g. the app was reinstalled) start again. */
    private suspend fun resumeLostDownloads() {
        val running = allTasks().mapNotNull { it.taskDescription }.toSet()
        val wifiOnly = preferences.downloadOnlyOnWifi.first()
        dao.observeVisible().first()
            .filter { it.status == DownloadStatus.QUEUED || it.status == DownloadStatus.DOWNLOADING }
            .filter { it.episodeId !in running }
            .forEach { row ->
                log.d { "Restarting download of ${row.episodeId}" }
                dao.updateStatus(row.episodeId, DownloadStatus.QUEUED)
                start(row.episodeId, row.audioUrl, wifiOnly)
            }
    }

    private suspend fun cancelTask(episodeId: String) {
        allTasks().filter { it.taskDescription == episodeId }.forEach { it.cancel() }
        progress.update { it - episodeId }
    }

    private suspend fun allTasks(): List<NSURLSessionTask> = suspendCancellableCoroutine { continuation ->
        session.getAllTasksWithCompletionHandler { tasks ->
            continuation.resume(tasks.orEmpty().filterIsInstance<NSURLSessionTask>())
        }
    }

    private suspend fun deleteNow(episodeId: String) {
        files.delete(episodeId)
        dao.delete(episodeId)
    }

    private inner class Delegate : NSObject(), NSURLSessionDownloadDelegateProtocol {

        /** Tasks that already reported progress (main queue only). */
        private val reportingTasks = mutableSetOf<ULong>()

        override fun URLSession(
            session: NSURLSession,
            downloadTask: NSURLSessionDownloadTask,
            didWriteData: Long,
            totalBytesWritten: Long,
            totalBytesExpectedToWrite: Long,
        ) {
            val episodeId = downloadTask.taskDescription ?: return
            // The first bytes of this task: queued → downloading.
            val firstReport = downloadTask.taskIdentifier !in reportingTasks
            if (firstReport) reportingTasks += downloadTask.taskIdentifier
            if (totalBytesExpectedToWrite > 0) {
                progress.update { it + (episodeId to (totalBytesWritten.toFloat() / totalBytesExpectedToWrite).coerceIn(0f, 1f)) }
            } else if (episodeId !in progress.value) {
                progress.update { it + (episodeId to null) }
            }
            if (firstReport) scope.launch { dao.updateStatus(episodeId, DownloadStatus.DOWNLOADING) }
        }

        override fun URLSession(
            session: NSURLSession,
            downloadTask: NSURLSessionDownloadTask,
            didFinishDownloadingToURL: NSURL,
        ) {
            val episodeId = downloadTask.taskDescription ?: return
            // The temporary file is gone once this returns: move it into place now.
            val status = (downloadTask.response as? platform.Foundation.NSHTTPURLResponse)?.statusCode ?: 200
            if (status !in 200..299) {
                log.w { "Download of $episodeId failed: HTTP $status" }
                scope.launch { dao.updateStatus(episodeId, DownloadStatus.FAILED) }
                return
            }
            val size = files.moveIntoPlace(episodeId, didFinishDownloadingToURL)
            progress.update { it - episodeId }
            scope.launch {
                if (size != null) dao.markDownloaded(episodeId, size) else dao.updateStatus(episodeId, DownloadStatus.FAILED)
            }
        }

        override fun URLSession(session: NSURLSession, task: NSURLSessionTask, didCompleteWithError: NSError?) {
            val episodeId = task.taskDescription ?: return
            val error = didCompleteWithError ?: return
            progress.update { it - episodeId }
            // Cancelled by the app (cancel, re-download, removal): the row is handled there.
            if (error.domain == NSURLErrorDomain && error.code == NSURLErrorCancelled) return
            log.w { "Download of $episodeId failed: ${error.localizedDescription}" }
            scope.launch { dao.updateStatus(episodeId, DownloadStatus.FAILED) }
        }

        override fun URLSessionDidFinishEventsForBackgroundURLSession(session: NSURLSession) {
            backgroundCompletionHandler?.invoke()
            backgroundCompletionHandler = null
        }
    }

    companion object {
        const val SESSION_ID = "com.propodcast.app.episode-downloads"

        /** Set by [handleBackgroundEvents]; called once the session has delivered its events. */
        private var backgroundCompletionHandler: (() -> Unit)? = null

        /**
         * From the app delegate's `application(_:handleEventsForBackgroundURLSession:completionHandler:)`:
         * iOS relaunched the app to finish downloads. The session (created with Koin) delivers the events,
         * then [completionHandler] tells iOS the app is done.
         */
        fun handleBackgroundEvents(identifier: String, completionHandler: () -> Unit) {
            if (identifier == SESSION_ID) backgroundCompletionHandler = completionHandler else completionHandler()
        }
    }
}

/**
 * Downloaded episodes in Application Support/episodes: persistent (the system does not purge it, unlike Caches
 * or tmp) and excluded from backups, since the files can be downloaded again.
 */
@OptIn(ExperimentalForeignApi::class)
private class IosEpisodeFileStore {
    private val fileManager = NSFileManager.defaultManager
    private val dir = persistentNonBackedUpDirectory("episodes")

    fun path(episodeId: String) = "$dir/${safeName(episodeId)}.mp3"

    fun exists(path: String) = fileManager.fileExistsAtPath(path)

    /** Moves the finished download into place; its size, or null when the move failed. */
    fun moveIntoPlace(episodeId: String, location: NSURL): Long? {
        val target = path(episodeId)
        if (exists(target)) fileManager.removeItemAtPath(target, error = null)
        val moved = fileManager.moveItemAtURL(location, toURL = NSURL.fileURLWithPath(target), error = null)
        if (!moved) return null
        val attributes = fileManager.attributesOfItemAtPath(target, error = null)
        return (attributes?.get(NSFileSize) as? NSNumber)?.longLongValue ?: 0
    }

    fun delete(episodeId: String) {
        val target = path(episodeId)
        if (exists(target)) fileManager.removeItemAtPath(target, error = null)
    }

    /** Episode ids are API ids, but keep file names safe anyway. */
    private fun safeName(episodeId: String) = episodeId.replace(Regex("[^A-Za-z0-9_-]"), "_")
}
