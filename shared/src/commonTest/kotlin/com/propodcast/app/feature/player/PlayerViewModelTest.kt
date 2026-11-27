package com.propodcast.app.feature.player

import com.propodcast.app.core.model.toLastPlayed
import com.propodcast.app.core.model.toNowPlaying
import com.propodcast.app.core.player.PlayerState
import com.propodcast.app.core.testing.FakePlayerController
import com.propodcast.app.core.testing.FakeUserPreferencesRepository
import com.propodcast.app.core.testing.MainDispatcherRule
import com.propodcast.app.core.testing.testEpisode
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

class PlayerViewModelTest {

    private val mainDispatcherRule = MainDispatcherRule()

    @BeforeTest
    fun setMainDispatcher() = mainDispatcherRule.start()

    @AfterTest
    fun resetMainDispatcher() = mainDispatcherRule.finish()

    private val controller = FakePlayerController()
    private val preferences = FakeUserPreferencesRepository()
    private val episode = testEpisode("e1").copy(durationSec = 600)

    /** uiState is WhileSubscribed: keep a collector so combine() runs. */
    private fun TestScope.subscribed() = PlayerViewModel(controller, preferences).also { vm ->
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
    }

    @Test
    fun nothingPlayedYet_isHidden() = runTest {
        assertFalse(subscribed().uiState.value.isVisible)
    }

    @Test
    fun lastPlayed_isShownAsRestored_andPlayResumesFromSavedPosition() = runTest {
        preferences.lastPlayed.value = episode.toNowPlaying()!!.toLastPlayed(positionMs = 120_000)
        val vm = subscribed()

        val state = vm.uiState.value
        assertTrue(state.isRestored)
        assertEquals("e1", state.playback.nowPlaying?.episodeId)
        assertEquals(0.2f, state.progress!!, 0.001f)

        vm.togglePlayPause()
        assertEquals(listOf("play e1 @120000"), controller.calls)
        assertFalse(vm.uiState.value.isRestored, "the live player replaces the restored episode")
    }

    @Test
    fun restoredEpisode_seekButtonsLoadItAroundSavedPosition() = runTest {
        preferences.lastPlayed.value = episode.toNowPlaying()!!.toLastPlayed(positionMs = 10_000)
        val vm = subscribed()

        vm.seekBack()
        assertEquals(listOf("play e1 @0"), controller.calls, "seeking back never goes below 0")
    }

    @Test
    fun playingAnotherEpisode_startsFromBeginning_sameEpisode_toggles() = runTest {
        val vm = subscribed()

        assertTrue(vm.play(episode))
        assertTrue(vm.play(episode))
        assertTrue(vm.play(testEpisode("e2")))

        assertEquals(listOf("play e1 @0", "toggle", "play e2 @0"), controller.calls)
    }

    @Test
    fun tappingTheRestoredEpisode_resumesIt() = runTest {
        preferences.lastPlayed.value = episode.toNowPlaying()!!.toLastPlayed(positionMs = 30_000)
        val vm = subscribed()

        vm.play(episode)

        assertEquals(listOf("play e1 @30000"), controller.calls)
    }

    @Test
    fun episodeWithoutAudio_isRejected() = runTest {
        val vm = subscribed()

        assertFalse(vm.play(episode.copy(audioUrl = null)))
        assertTrue(controller.calls.isEmpty())
    }

    @Test
    fun liveControls_goToTheController() = runTest {
        controller.state.value = PlayerState(nowPlaying = episode.toNowPlaying(), isPlaying = true)
        val vm = subscribed()

        vm.togglePlayPause()
        vm.seekBack()
        vm.seekForward()
        vm.seekTo(5_000)
        vm.stop()

        assertEquals(listOf("toggle", "back", "forward", "seekTo 5000", "stop"), controller.calls)
    }

    @Test
    fun setSpeed_goesToTheController() = runTest {
        controller.state.value = PlayerState(nowPlaying = episode.toNowPlaying())
        val vm = subscribed()

        vm.setSpeed(1.75f)

        assertEquals(listOf("speed 1.75"), controller.calls)
        assertEquals(1.75f, vm.uiState.value.playback.speed)
    }
}
