package com.propodcast.app.feature.favorites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.runComposeUiTest
import com.propodcast.app.core.testing.UiTestBase
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
import kotlin.test.assertEquals
import kotlin.test.Test

class FavoritesContentTest : UiTestBase() {

    private fun ComposeUiTest.podcast(id: String) = Podcast(
        id = id,
        title = "Podcast $id",
        publisher = "Publisher",
        imageUrl = null,
        thumbnailUrl = null,
        description = "",
        totalEpisodes = 5,
        listennotesUrl = "https://www.listennotes.com/c/$id/",
    )

    private fun ComposeUiTest.episode(id: String) = Episode(
        id = id,
        title = "Episode $id",
        description = "",
        audioUrl = null,
        imageUrl = null,
        thumbnailUrl = null,
        pubDateMs = 0,
        durationSec = 60,
        listennotesUrl = null,
        podcastId = "p1",
        podcastTitle = "Podcast p1",
    )

    private val clicked = mutableListOf<String>()

    private fun ComposeUiTest.showContent(state: FavoritesUiState) {
        setContent {
            ProPodcastTheme {
                FavoritesContent(
                    state = state,
                    onPodcastClick = { clicked += "open ${it.id}" },
                    onRemovePodcast = { clicked += "remove ${it.id}" },
                    onSharePodcast = { clicked += "share ${it.id}" },
                    onEpisodeClick = { clicked += "play ${it.id}" },
                    onRemoveEpisode = { clicked += "remove ${it.id}" },
                    onShareEpisode = { clicked += "share ${it.id}" },
                )
            }
        }
    }

    @Test
    fun emptyTabs_showHints() = runComposeUiTest {
        showContent(FavoritesUiState(isLoading = false))

        onNodeWithText("No favorite podcasts yet.", substring = true).assertIsDisplayed()
        onNodeWithText("Episodes").performClick()
        onNodeWithText("No favorite episodes yet.", substring = true).assertIsDisplayed()
    }

    @Test
    fun tabsShowCounts_andSwitchPages() = runComposeUiTest {
        showContent(FavoritesUiState(listOf(podcast("p1"), podcast("p2")), listOf(episode("e1")), isLoading = false))

        onNodeWithText("Podcasts (2)").assertIsDisplayed()
        onNodeWithText("Podcast p1").performClick()
        onNodeWithText("Episodes (1)").performClick()
        onNodeWithText("Episode e1").performClick()

        assertEquals(listOf("open p1", "play e1"), clicked)
    }

    @Test
    fun heartButton_removes() = runComposeUiTest {
        showContent(FavoritesUiState(listOf(podcast("p1")), emptyList(), isLoading = false))

        onAllNodesWithContentDescription("Remove from favorites")[0].performClick()

        assertEquals(listOf("remove p1"), clicked)
    }

    @Test
    fun swipeEitherWay_removes() = runComposeUiTest {
        showContent(FavoritesUiState(listOf(podcast("p1"), podcast("p2")), emptyList(), isLoading = false))

        onNodeWithText("Podcast p1").performTouchInput { swipeLeft() }
        onNodeWithText("Podcast p2").performTouchInput { swipeRight() }
        waitForIdle()

        assertEquals(listOf("remove p1", "remove p2"), clicked)
    }

    /** Real flow: a swiped row is removed, then Undo puts the same item (same key) back. */
    private fun ComposeUiTest.setStatefulContent(initial: List<Podcast>): (List<Podcast>) -> Unit {
        var podcasts by mutableStateOf(initial)
        setContent {
            ProPodcastTheme {
                FavoritesContent(
                    state = FavoritesUiState(podcasts, emptyList(), isLoading = false),
                    onPodcastClick = {},
                    onRemovePodcast = { removed ->
                        clicked += "remove ${removed.id}"
                        podcasts = podcasts - removed
                    },
                    onSharePodcast = {},
                    onEpisodeClick = {},
                    onRemoveEpisode = {},
                    onShareEpisode = {},
                )
            }
        }
        return { podcasts = it }
    }

    @Test
    fun undoRightAfterSwipe_keepsRestoredRow() = runComposeUiTest {
        val p1 = podcast("p1")
        val setPodcasts = setStatefulContent(listOf(p1, podcast("p2")))

        onNodeWithText("Podcast p1").performTouchInput { swipeLeft() }
        waitUntil { clicked.isNotEmpty() }
        mainClock.autoAdvance = false
        mainClock.advanceTimeBy(32) // the removal is drawn; a fade-out would still be running
        setPodcasts(listOf(p1, podcast("p2"))) // Undo
        mainClock.autoAdvance = true
        waitForIdle()

        assertEquals(listOf("remove p1"), clicked, "restored row must not be removed again")
        onNodeWithText("Podcast p1").assertIsDisplayed()
    }

    @Test
    fun undoAfterRowIsGone_keepsRestoredRow_andItCanBeSwipedAgain() = runComposeUiTest {
        val p1 = podcast("p1")
        val setPodcasts = setStatefulContent(listOf(p1, podcast("p2")))

        onNodeWithText("Podcast p1").performTouchInput { swipeLeft() }
        waitForIdle()
        mainClock.advanceTimeBy(2_000)
        setPodcasts(listOf(p1, podcast("p2"))) // Undo
        waitForIdle()

        assertEquals(listOf("remove p1"), clicked, "restored row must not be removed again")
        onNodeWithText("Podcast p1").assertIsDisplayed().performTouchInput { swipeLeft() }
        waitForIdle()
        assertEquals(listOf("remove p1", "remove p1"), clicked)
    }

    @Test
    fun loading_hidesLists() = runComposeUiTest {
        showContent(FavoritesUiState())
        onNodeWithTag(FAVORITE_PODCASTS_LIST_TAG).assertDoesNotExist()
    }
}
