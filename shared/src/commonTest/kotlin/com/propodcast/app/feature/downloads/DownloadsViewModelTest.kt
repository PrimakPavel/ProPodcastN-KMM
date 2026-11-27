package com.propodcast.app.feature.downloads

import app.cash.turbine.test
import com.propodcast.app.resources.Res
import com.propodcast.app.resources.download_cancelled
import com.propodcast.app.resources.download_deleted
import com.propodcast.app.resources.download_retry
import com.propodcast.app.resources.download_waiting_wifi
import com.propodcast.app.core.connectivity.ConnectionType
import com.propodcast.app.core.testing.FakeConnectivityMonitor
import com.propodcast.app.core.testing.FakeDownloadsRepository
import com.propodcast.app.core.testing.FakeUserPreferencesRepository
import com.propodcast.app.core.testing.MainDispatcherRule
import com.propodcast.app.core.testing.testEpisode
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class DownloadsViewModelTest {

    private val mainDispatcherRule = MainDispatcherRule()

    @BeforeTest
    fun setMainDispatcher() = mainDispatcherRule.start()

    @AfterTest
    fun resetMainDispatcher() = mainDispatcherRule.finish()

    private val repository = FakeDownloadsRepository()
    private val connectivity = FakeConnectivityMonitor()
    private val preferences = FakeUserPreferencesRepository()
    // Lazy: created after MainDispatcherRule has swapped Dispatchers.Main (viewModelScope uses it).
    private val viewModel by lazy { DownloadsViewModel(repository, connectivity, preferences) }
    private val episode = testEpisode("e1")

    @Test
    fun onWifi_downloadsRightAway() = runTest {
        viewModel.download(episode)

        assertEquals(listOf("enqueue e1"), repository.calls)
        assertNull(viewModel.confirmation.value)
    }

    @Test
    fun withoutNetwork_queuesForLater() = runTest {
        connectivity.connection.value = ConnectionType.NONE

        viewModel.download(episode)

        assertEquals(listOf("enqueue e1"), repository.calls)
    }

    @Test
    fun onMobileData_asksFirst_thenDownloadsOnConfirm() = runTest {
        connectivity.connection.value = ConnectionType.METERED

        viewModel.download(episode)
        assertEquals(episode, viewModel.confirmation.value)
        assertTrue(repository.calls.isEmpty(), "nothing starts before the answer")

        viewModel.confirmDownload()
        assertEquals(listOf("enqueue e1"), repository.calls)
        assertNull(viewModel.confirmation.value)
    }

    @Test
    fun onMobileData_wifiOnly_queuesWithoutAsking_andSaysItWaitsForWifi() = runTest {
        connectivity.connection.value = ConnectionType.METERED
        preferences.setDownloadOnlyOnWifi(true)

        viewModel.snackbarEvents.test {
            viewModel.download(episode)

            assertEquals(Res.string.download_waiting_wifi, awaitItem().messageRes)
            assertEquals(listOf("enqueue e1"), repository.calls)
            assertNull(viewModel.confirmation.value, "no mobile data dialog")
        }
    }

    @Test
    fun onMobileData_dismissedDialog_downloadsNothing() = runTest {
        connectivity.connection.value = ConnectionType.METERED

        viewModel.download(episode)
        viewModel.dismissConfirmation()

        assertTrue(repository.calls.isEmpty())
        assertNull(viewModel.confirmation.value)
    }

    @Test
    fun episodeWithoutAudio_isIgnored() = runTest {
        viewModel.download(episode.copy(audioUrl = null))
        assertTrue(repository.calls.isEmpty())
    }

    @Test
    fun cancel_offersRetry() = runTest {
        viewModel.snackbarEvents.test {
            viewModel.cancel(episode)
            val event = awaitItem()
            assertEquals(Res.string.download_cancelled, event.messageRes)
            assertEquals(Res.string.download_retry, event.actionLabelRes)

            event.onAction()
            assertEquals(listOf("cancel e1", "enqueue e1"), repository.calls)
        }
    }

    @Test
    fun delete_hidesFirst_undoBringsBack_dismissDeletesTheFile() = runTest {
        viewModel.snackbarEvents.test {
            viewModel.delete(episode)
            val event = awaitItem()
            assertEquals(Res.string.download_deleted, event.messageRes)
            assertEquals(listOf("markRemoved e1"), repository.calls)

            event.onAction()
            event.onDismiss()
            assertEquals(listOf("markRemoved e1", "undoRemove e1", "commitRemove e1"), repository.calls)
        }
    }
}
