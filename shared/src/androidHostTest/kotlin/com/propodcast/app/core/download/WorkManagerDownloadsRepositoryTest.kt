package com.propodcast.app.core.download

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.propodcast.app.core.database.ProPodcastDatabase
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.testing.FakeAnalyticsTracker
import com.propodcast.app.core.testing.FakeUserPreferencesRepository
import com.propodcast.app.core.testing.TestClock
import com.propodcast.app.core.testing.testEpisode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Real Room + WorkManager's test driver (network constraint unmet, so work stays queued). */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class WorkManagerDownloadsRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var db: ProPodcastDatabase
    private lateinit var workManager: WorkManager
    private lateinit var files: EpisodeFileStore
    private lateinit var repository: WorkManagerDownloadsRepository
    private val analytics = FakeAnalyticsTracker()
    private val preferences = FakeUserPreferencesRepository()

    @Before
    fun setUp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setExecutor(SynchronousExecutor()).build(),
        )
        workManager = WorkManager.getInstance(context)
        db = Room.inMemoryDatabaseBuilder<ProPodcastDatabase>(context).allowMainThreadQueries().build()
        files = EpisodeFileStore(context)
        repository = WorkManagerDownloadsRepository(db.downloadedEpisodeDao(), workManager, files, analytics, preferences, TestClock(1_000))
    }

    @After
    fun tearDown() {
        db.close()
        files.delete("e1")
    }

    private fun workInfo(): WorkInfo =
        workManager.getWorkInfosForUniqueWork(EpisodeDownloadWorker.uniqueWorkName("e1")).get().single()

    private fun workState() = workInfo().state

    @Test
    fun enqueue_queuesTheWork_andListsTheEpisode() = runTest {
        repository.enqueue(testEpisode("e1"))

        assertEquals(WorkInfo.State.ENQUEUED, workState())
        assertEquals(listOf("download_episode"), analytics.events)
        val download = repository.downloads.first().single()
        assertEquals("e1", download.episode.id)
        assertEquals(DownloadStatus.QUEUED, download.state.status)
        assertTrue(download.state.isActive)
    }

    @Test
    fun downloadOnlyOnWifi_waitsForAnUnmeteredNetwork() = runTest {
        repository.enqueue(testEpisode("e1"))
        assertEquals(NetworkType.CONNECTED, workInfo().constraints.requiredNetworkType)

        preferences.setDownloadOnlyOnWifi(true)
        repository.enqueue(testEpisode("e1"))
        assertEquals(NetworkType.UNMETERED, workInfo().constraints.requiredNetworkType)
    }

    @Test
    fun cancel_stopsTheWork_andForgetsTheDownload() = runTest {
        repository.enqueue(testEpisode("e1"))

        repository.cancel("e1")

        assertEquals(WorkInfo.State.CANCELLED, workState())
        assertTrue(repository.downloads.first().isEmpty())
    }

    @Test
    fun removal_hidesDuringUndo_thenDeletesTheFile() = runTest {
        repository.enqueue(testEpisode("e1"))
        db.downloadedEpisodeDao().markDownloaded("e1", sizeBytes = 3)
        files.file("e1").writeText("mp3")
        assertEquals(files.file("e1").absolutePath, repository.localFilePath("e1"))

        repository.markRemoved("e1")
        assertTrue("hidden while Undo is offered", repository.downloads.first().isEmpty())
        assertNull("not played from a file that is going away", repository.localFilePath("e1"))

        repository.undoRemove("e1")
        assertEquals(1, repository.downloads.first().size)

        repository.markRemoved("e1")
        repository.commitRemove("e1")
        assertFalse(files.file("e1").exists())
        assertNull(db.downloadedEpisodeDao().get("e1"))
    }

    @Test
    fun localFile_onlyForFinishedDownloads() = runTest {
        repository.enqueue(testEpisode("e1"))
        files.file("e1").writeText("partial?")

        assertNull(repository.localFilePath("e1"))
    }
}
