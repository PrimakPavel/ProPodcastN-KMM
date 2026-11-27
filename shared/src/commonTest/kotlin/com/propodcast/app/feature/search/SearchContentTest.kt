package com.propodcast.app.feature.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.runComposeUiTest
import com.propodcast.app.core.testing.UiTestBase
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.model.Podcast
import kotlinx.coroutines.flow.flowOf
import kotlin.test.assertTrue
import kotlin.test.Test

class SearchContentTest : UiTestBase() {

    private val done = LoadState.NotLoading(endOfPaginationReached = true)

    private fun ComposeUiTest.podcast(id: String, title: String) = Podcast(
        id = id,
        title = title,
        publisher = "Publisher",
        imageUrl = null,
        thumbnailUrl = null,
        description = "",
        totalEpisodes = 1,
        listennotesUrl = null,
    )

    /** Real text state, so typing in the field updates the content like the ViewModel would. */
    private fun ComposeUiTest.showContent(
        initialQuery: String = "",
        language: String? = null,
        results: List<Podcast> = emptyList(),
        onOpenLanguages: () -> Unit = {},
    ) {
        setContent {
            var query by remember { mutableStateOf(initialQuery) }
            ProPodcastTheme {
                SearchContent(
                    query = query,
                    language = language,
                    results = flowOf(PagingData.from(results, LoadStates(done, done, done))).collectAsLazyPagingItems(),
                    favoriteIds = emptySet(),
                    onQueryChange = { query = it },
                    onOpenLanguages = onOpenLanguages,
                    onPodcastClick = {},
                    onToggleFavorite = {},
                    onShare = {},
                )
            }
        }
    }

    @Test
    fun hintsChangeWithQueryLength() = runComposeUiTest {
        showContent()
        onNodeWithText("Find podcasts by name, topic or publisher.").assertIsDisplayed()

        onNodeWithTag(SEARCH_FIELD_TAG).performTextInput("st")
        onNodeWithText("Type at least 3 characters to search.").assertIsDisplayed()
        onNodeWithText("Find podcasts by name, topic or publisher.").assertDoesNotExist()
    }

    @Test
    fun searchableQuery_showsResults_andClearReturnsToStart() = runComposeUiTest {
        showContent(initialQuery = "star", results = listOf(podcast("1", "Star Wars 7x7")))
        onNodeWithText("Star Wars 7x7").assertIsDisplayed()

        onNodeWithContentDescription("Clear search").performClick()

        onNodeWithText("Find podcasts by name, topic or publisher.").assertIsDisplayed()
    }

    @Test
    fun noResults_showsEmptyMessageWithQuery() = runComposeUiTest {
        showContent(initialQuery = "zzzz ")

        onNodeWithText("Nothing found for “zzzz”.").assertIsDisplayed()
    }

    @Test
    fun languageChip_showsSelectionAndOpensSheet() = runComposeUiTest {
        var opened = false
        showContent(language = "Ukranian", onOpenLanguages = { opened = true })

        onNodeWithText("Ukranian").performClick()

        assertTrue(opened)
    }

    @Test
    fun noLanguage_showsAnyLanguage() = runComposeUiTest {
        showContent()

        onNodeWithText("Any language").assertIsDisplayed()
    }
}
