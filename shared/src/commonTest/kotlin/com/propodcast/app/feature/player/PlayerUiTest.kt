package com.propodcast.app.feature.player

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.runComposeUiTest
import com.propodcast.app.core.testing.UiTestBase
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.model.NowPlaying
import com.propodcast.app.core.player.PlayerError
import com.propodcast.app.core.player.PlayerState
import kotlin.test.assertEquals
import kotlin.test.Test

class PlayerUiTest : UiTestBase() {

    private val item = NowPlaying(
        episodeId = "e1",
        title = "Episode one",
        podcastTitle = "The Podcast",
        audioUrl = "https://example.com/e1.mp3",
        imageUrl = null,
        durationMs = 3_723_000,
    )
    private val calls = mutableListOf<String>()

    private fun ComposeUiTest.setMiniPlayer(state: PlayerUiState) = setContent {
        ProPodcastTheme {
            MiniPlayer(
                state = state,
                onOpen = { calls += "open" },
                onTogglePlayPause = { calls += "toggle" },
                onClose = { calls += "close" },
            )
        }
    }

    private fun ComposeUiTest.setFullPlayer(state: PlayerUiState) = setContent {
        ProPodcastTheme {
            FullPlayerContent(
                state = state,
                onCollapse = { calls += "collapse" },
                onTogglePlayPause = { calls += "toggle" },
                onSeekTo = { calls += "seekTo $it" },
                onSeekBack = { calls += "back" },
                onSeekForward = { calls += "forward" },
                onSpeedSelected = { calls += "speed $it" },
            )
        }
    }

    @Test
    fun miniPlayer_hiddenWithoutEpisode() = runComposeUiTest {
        setMiniPlayer(PlayerUiState())
        onNodeWithTag(MINI_PLAYER_TAG).assertDoesNotExist()
    }

    @Test
    fun miniPlayer_showsEpisode_andForwardsClicks() = runComposeUiTest {
        setMiniPlayer(PlayerUiState(PlayerState(nowPlaying = item, isPlaying = true)))

        onNodeWithText("Episode one").assertIsDisplayed()
        onNodeWithText("The Podcast").assertIsDisplayed()
        onNodeWithContentDescription("Pause").performClick()
        onNodeWithContentDescription("Close player").performClick()
        onNodeWithText("Episode one").performClick()

        assertEquals(listOf("toggle", "close", "open"), calls)
    }

    @Test
    fun miniPlayer_restoredEpisode_offersToContinue() = runComposeUiTest {
        setMiniPlayer(PlayerUiState(PlayerState(nowPlaying = item, positionMs = 60_000), isRestored = true))

        onNodeWithText("Continue listening").assertIsDisplayed()
        onNodeWithContentDescription("Play").assertIsDisplayed()
    }

    @Test
    fun miniPlayer_showsError() = runComposeUiTest {
        setMiniPlayer(PlayerUiState(PlayerState(nowPlaying = item, error = PlayerError.NETWORK)))

        onNodeWithText("Can’t load the episode. Check your connection.").assertIsDisplayed()
    }

    @Test
    fun fullPlayer_showsTimes_andForwardsControls() = runComposeUiTest {
        setFullPlayer(PlayerUiState(PlayerState(nowPlaying = item, positionMs = 61_000, durationMs = 3_723_000, speed = 1.5f)))

        onNodeWithText("1:01").assertIsDisplayed()
        onNodeWithText("-1:01:02").assertIsDisplayed()
        onNodeWithContentDescription("Back 15 seconds").performClick()
        onNodeWithContentDescription("Play").performClick()
        onNodeWithContentDescription("Forward 30 seconds").performClick()
        onNodeWithContentDescription("Playback speed 1.5×").performClick()
        onNodeWithText("2×").performClick()
        onNodeWithContentDescription("Collapse player").performClick()

        assertEquals(listOf("back", "toggle", "forward", "speed 2.0", "collapse"), calls)
    }

    @Test
    fun fullPlayer_unknownDuration_disablesSeekBar() = runComposeUiTest {
        setFullPlayer(PlayerUiState(PlayerState(nowPlaying = item.copy(durationMs = 0))))

        onNodeWithTag(PLAYER_SEEK_BAR_TAG).assertIsNotEnabled()
    }
}
