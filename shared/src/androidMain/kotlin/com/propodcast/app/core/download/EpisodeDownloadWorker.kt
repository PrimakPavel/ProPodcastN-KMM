package com.propodcast.app.core.download

import co.touchlab.kermit.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.propodcast.app.core.database.dao.DownloadedEpisodeDao
import com.propodcast.app.core.model.DownloadStatus
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink

/**
 * Downloads one episode to [EpisodeFileStore] as a foreground job with a progress notification.
 * Network errors are retried with backoff; after [MAX_ATTEMPTS] the download is marked failed.
 * Cancelling the work (from the app or the notification) removes the partial file.
 */
class EpisodeDownloadWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    // WorkManager creates workers by reflection; dependencies come from Koin.
    private val dao: DownloadedEpisodeDao by inject()
    private val files: EpisodeFileStore by inject()
    private val notifications: DownloadNotifications by inject()
    private val client: OkHttpClient by inject(DOWNLOAD_CLIENT)
    private val log = Logger.withTag("EpisodeDownloadWorker")

    override suspend fun doWork(): Result {
        val episodeId = inputData.getString(KEY_EPISODE_ID) ?: return Result.failure()
        // No row: the download was cancelled before the work started.
        val episode = dao.get(episodeId) ?: return Result.failure()
        val cancelIntent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)
        suspend fun showProgress(progress: Float?) {
            try {
                setForeground(notifications.foregroundInfo(episodeId, episode.title, progress, cancelIntent))
            } catch (e: IllegalStateException) {
                // Not allowed to go foreground right now (background start limits): keep downloading anyway.
                log.w(e) { "Download runs without a foreground notification" }
            }
        }

        showProgress(null)
        dao.updateStatus(episodeId, DownloadStatus.DOWNLOADING)
        return try {
            download(episode.audioUrl, files.partFile(episodeId)) { progress ->
                setProgress(workDataOf(KEY_PROGRESS to progress))
                showProgress(progress)
            }
            val file = files.commit(episodeId)
            dao.markDownloaded(episodeId, file.length())
            Result.success()
        } catch (e: CancellationException) {
            files.deletePart(episodeId)
            throw e
        } catch (e: IOException) {
            log.w(e) { "Download of $episodeId failed (attempt ${runAttemptCount + 1})" }
            files.deletePart(episodeId)
            if (runAttemptCount + 1 < MAX_ATTEMPTS) {
                dao.updateStatus(episodeId, DownloadStatus.QUEUED)
                Result.retry()
            } else {
                dao.updateStatus(episodeId, DownloadStatus.FAILED)
                Result.failure()
            }
        }
    }

    /** Streams [url] into [target], reporting progress (0..1) at most every [PROGRESS_INTERVAL_MS]. */
    private suspend fun download(url: String, target: File, onProgress: suspend (Float) -> Unit) =
        withContext(Dispatchers.IO) {
            client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                val body = response.body
                val total = body.contentLength()
                var written = 0L
                var lastReport = 0L
                body.source().use { source ->
                    target.sink().buffer().use { sink ->
                        while (true) {
                            ensureActive()
                            val read = source.read(sink.buffer, CHUNK_BYTES)
                            if (read == -1L) break
                            sink.emitCompleteSegments()
                            written += read
                            val now = System.currentTimeMillis()
                            if (total > 0 && now - lastReport >= PROGRESS_INTERVAL_MS) {
                                lastReport = now
                                onProgress((written.toFloat() / total).coerceIn(0f, 1f))
                            }
                        }
                    }
                }
                if (total > 0 && written < total) throw IOException("Connection closed at $written of $total bytes")
            }
        }

    companion object {
        const val TAG = "episode-download"

        /** OkHttpClient for episode audio files: no Listen Notes key, longer timeouts. */
        val DOWNLOAD_CLIENT = named("downloadClient")
        const val KEY_PROGRESS = "progress"
        private const val KEY_EPISODE_ID = "episode_id"
        private const val EPISODE_TAG_PREFIX = "episode:"
        private const val MAX_ATTEMPTS = 3
        private const val CHUNK_BYTES = 64 * 1024L
        private const val PROGRESS_INTERVAL_MS = 500L

        fun uniqueWorkName(episodeId: String) = "download-$episodeId"

        fun episodeIdFromTags(tags: Set<String>): String? =
            tags.firstOrNull { it.startsWith(EPISODE_TAG_PREFIX) }?.removePrefix(EPISODE_TAG_PREFIX)

        /** [wifiOnly]: waits for an unmetered network (Wi-Fi / Ethernet) instead of any connection. */
        fun request(episodeId: String, wifiOnly: Boolean = false): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<EpisodeDownloadWorker>()
                .setInputData(workDataOf(KEY_EPISODE_ID to episodeId))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
                .addTag(TAG)
                .addTag(EPISODE_TAG_PREFIX + episodeId)
                .build()

        private const val BACKOFF_SECONDS = 15L
    }
}
