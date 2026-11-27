package com.propodcast.app.feature.favorites

import app.cash.turbine.test
import com.propodcast.app.resources.Res
import com.propodcast.app.resources.action_undo
import com.propodcast.app.resources.favorite_removed
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.model.EpisodeDownload
import com.propodcast.app.core.model.EpisodeDownloadState
import com.propodcast.app.core.testing.FakeDownloadsRepository
import com.propodcast.app.core.testing.FakeFavoritesRepository
import com.propodcast.app.core.testing.MainDispatcherRule
import com.propodcast.app.core.testing.testEpisode
import com.propodcast.app.core.testing.testPodcast
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class FavoritesViewModelTest {

    private val mainDispatcherRule = MainDispatcherRule()

    @BeforeTest
    fun setMainDispatcher() = mainDispatcherRule.start()

    @AfterTest
    fun resetMainDispatcher() = mainDispatcherRule.finish()

    private val favorites = FakeFavoritesRepository()
    private val downloads = FakeDownloadsRepository()

    /** uiState is WhileSubscribed: keep a collector so combine() runs. */
    private fun TestScope.subscribed() = FavoritesViewModel(favorites, downloads).also { vm ->
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
    }

    @Test
    fun loading_untilRoomEmits_thenNewestFirst() = runTest {
        assertTrue(FavoritesViewModel(favorites, downloads).uiState.value.isLoading)

        favorites.setPodcastFavorite(testPodcast("p1"), true)
        favorites.setPodcastFavorite(testPodcast("p2"), true)
        favorites.setEpisodeFavorite(testEpisode("e1"), true)
        val vm = subscribed()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf("p2", "p1"), state.podcasts.map { it.id })
        assertEquals(listOf("e1"), state.episodes.map { it.id })
    }

    @Test
    fun removePodcast_offersUndo_thatRestoresSamePosition() = runTest {
        listOf("p1", "p2", "p3").forEach { favorites.setPodcastFavorite(testPodcast(it), true) }
        val vm = subscribed()

        vm.snackbarEvents.test {
            vm.removePodcast(testPodcast("p2"))
            val event = awaitItem()
            assertEquals(Res.string.favorite_removed, event.messageRes)
            assertEquals(listOf("Podcast p2"), event.messageArgs)
            assertEquals(Res.string.action_undo, event.actionLabelRes)
            assertEquals(listOf("p3", "p1"), vm.uiState.value.podcasts.map { it.id })

            event.onAction()
            assertEquals(listOf("p3", "p2", "p1"), vm.uiState.value.podcasts.map { it.id }, "undo keeps the original order")
        }
    }

    @Test
    fun removeEpisode_offersUndo() = runTest {
        favorites.setEpisodeFavorite(testEpisode("e1"), true)
        val vm = subscribed()

        vm.snackbarEvents.test {
            vm.removeEpisode(testEpisode("e1"))
            assertEquals(listOf("Episode e1"), awaitItem().messageArgs)
            assertTrue(vm.uiState.value.episodes.isEmpty())
        }
    }

    @Test
    fun downloads_runningFirst() = runTest {
        val running = EpisodeDownload(
            testEpisode("e2"),
            EpisodeDownloadState(DownloadStatus.DOWNLOADING),
            addedAtMs = 1,
        )
        val done = running.copy(
            episode = testEpisode("e1"),
            state = EpisodeDownloadState(DownloadStatus.DOWNLOADED),
        )
        downloads.downloads.value = listOf(done, running)
        val vm = subscribed()

        assertEquals(listOf("e2", "e1"), vm.uiState.value.downloads.map { it.episode.id })
    }

    @Test
    fun removingMissingFavorite_emitsNothing() = runTest {
        val vm = subscribed()
        vm.snackbarEvents.test {
            vm.removePodcast(testPodcast("nope"))
            expectNoEvents()
        }
    }
}
