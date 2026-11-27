package com.propodcast.app.feature.details

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.runComposeUiTest
import com.propodcast.app.core.testing.UiTestBase
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.propodcast.app.core.common.AppError
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test

class PodcastDetailsContentTest : UiTestBase() {

    private val podcast = Podcast(
        id = "p1",
        title = "Star Wars 7x7",
        publisher = "Allen Voivod",
        imageUrl = null,
        thumbnailUrl = null,
        description = "<p>Daily <b>Star Wars</b> joy.</p>",
        totalEpisodes = 3211,
        listennotesUrl = "https://www.listennotes.com/c/p1/",
    )

    private fun ComposeUiTest.episode(id: String) = Episode(
        id = id,
        title = "Episode $id",
        description = "",
        audioUrl = null,
        imageUrl = null,
        thumbnailUrl = null,
        pubDateMs = 0,
        durationSec = 2447,
        listennotesUrl = null,
        podcastId = "p1",
        podcastTitle = "Star Wars 7x7",
    )

    private fun ComposeUiTest.showContent(
        state: PodcastDetailsUiState,
        onRetry: () -> Unit = {},
        onLoadMore: () -> Unit = {},
        onPodcastClick: (Podcast) -> Unit = {},
        onToggleEpisodeFavorite: (Episode) -> Unit = {},
    ) {
        setContent {
            ProPodcastTheme {
                PodcastDetailsContent(
                    state = state,
                    onBack = {},
                    onRetry = onRetry,
                    onLoadMore = onLoadMore,
                    onTogglePodcastFavorite = {},
                    onSharePodcast = {},
                    onEpisodeClick = {},
                    onToggleEpisodeFavorite = onToggleEpisodeFavorite,
                    onShareEpisode = {},
                    onPodcastClick = onPodcastClick,
                )
            }
        }
    }

    @Test
    fun appBarHasNoTitle_headerHasIt() = runComposeUiTest {
        val loaded = PodcastDetailsUiState(content = DetailsContent.Loaded(podcast, emptyList(), nextEpisodePubDate = null))

        showContent(state = loaded)

        onAllNodesWithText("Star Wars 7x7").assertCountEquals(1)
    }

    @Test
    fun loaded_showsHeaderHtmlDescriptionEpisodesAndLoadMore() = runComposeUiTest {
        var loadMoreClicks = 0
        showContent(
            state = PodcastDetailsUiState(
                content = DetailsContent.Loaded(podcast, listOf(episode("1"), episode("2")), nextEpisodePubDate = 5L),
                isPodcastFavorite = true,
            ),
            onLoadMore = { loadMoreClicks++ },
        )

        onNodeWithText("Allen Voivod").assertIsDisplayed()
        onNodeWithText("3211 episodes").assertIsDisplayed()
        onNodeWithText("Daily Star Wars joy.", substring = true).assertIsDisplayed() // HTML tags rendered, not shown
        onNodeWithContentDescription("Remove from favorites").assertIsDisplayed()
        onNodeWithText("Episode 1").assertIsDisplayed()
        onAllNodesWithText("41 min", substring = true)[0].assertIsDisplayed()

        onNodeWithTag(DETAILS_LIST_TAG).performScrollToNode(hasText("Load more episodes"))
        onNodeWithText("Load more episodes").performClick()
        assertEquals(1, loadMoreClicks)
    }

    @Test
    fun allEpisodesLoaded_noLoadMoreButton() = runComposeUiTest {
        showContent(PodcastDetailsUiState(DetailsContent.Loaded(podcast, listOf(episode("1")), nextEpisodePubDate = null)))

        onNodeWithText("Load more episodes").assertDoesNotExist()
    }

    @Test
    fun loadMoreError_showsRetryInFooter() = runComposeUiTest {
        var retried = false
        showContent(
            state = PodcastDetailsUiState(
                DetailsContent.Loaded(podcast, listOf(episode("1")), nextEpisodePubDate = 5L, loadMoreError = AppError.Network),
            ),
            onLoadMore = { retried = true },
        )

        onNodeWithText("No internet connection.").assertIsDisplayed()
        onNodeWithText("Retry").performClick()
        assertTrue(retried)
    }

    @Test
    fun error_showsMessageAndRetry() = runComposeUiTest {
        var retried = false
        showContent(PodcastDetailsUiState(DetailsContent.Error(AppError.NotFound)), onRetry = { retried = true })

        onNodeWithText("Nothing was found.").assertIsDisplayed()
        onNodeWithText("Retry").performClick()
        assertTrue(retried)
    }

    @Test
    fun recommendationClick_opensPodcast() = runComposeUiTest {
        var opened: Podcast? = null
        showContent(
            state = PodcastDetailsUiState(
                content = DetailsContent.Loaded(podcast, emptyList(), nextEpisodePubDate = null),
                recommendations = listOf(podcast.copy(id = "r1", title = "Recommended show")),
            ),
            onPodcastClick = { opened = it },
        )

        onNodeWithText("You may also like").assertIsDisplayed()
        onNodeWithText("Recommended show").performClick()
        assertEquals("r1", opened?.id)
        onNodeWithText("No episodes yet.").assertIsDisplayed()
    }
}
