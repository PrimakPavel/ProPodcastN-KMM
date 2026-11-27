package com.propodcast.app.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.runComposeUiTest
import com.propodcast.app.core.testing.UiTestBase
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.propodcast.app.core.common.AppError
import com.propodcast.app.core.common.AppException
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.model.ApiQuota
import com.propodcast.app.core.model.Genre
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.model.PodcastFilters
import com.propodcast.app.core.ui.OptionsState
import com.propodcast.app.feature.filters.FiltersTab
import com.propodcast.app.feature.filters.FiltersUiState
import kotlinx.coroutines.flow.flowOf
import kotlin.test.assertEquals
import kotlin.test.Test

class HomeContentTest : UiTestBase() {

    private val done = LoadState.NotLoading(endOfPaginationReached = true)

    private fun ComposeUiTest.podcast(id: String, title: String) = Podcast(
        id = id,
        title = title,
        publisher = "Publisher $id",
        imageUrl = null,
        thumbnailUrl = null,
        description = "",
        totalEpisodes = 3,
        listennotesUrl = "https://www.listennotes.com/c/$id/",
    )

    private fun ComposeUiTest.showContent(
        data: PagingData<Podcast>,
        favoriteIds: Set<String> = emptySet(),
        quota: ApiQuota? = null,
        filters: FiltersUiState = FiltersUiState(),
        onToggleFavorite: (Podcast) -> Unit = {},
        onOpenFilters: (FiltersTab) -> Unit = {},
        onClearGenre: () -> Unit = {},
        filtersPinned: Boolean = false,
        onOpenDownloads: (() -> Unit)? = null,
    ) {
        setContent {
            ProPodcastTheme {
                HomeContent(
                    podcasts = flowOf(data).collectAsLazyPagingItems(),
                    favoriteIds = favoriteIds,
                    quota = quota,
                    filtersState = filters,
                    onPodcastClick = {},
                    onToggleFavorite = onToggleFavorite,
                    onShare = {},
                    onOpenFilters = onOpenFilters,
                    onClearGenre = onClearGenre,
                    onClearRegion = {},
                    filtersPinned = filtersPinned,
                    onOpenDownloads = onOpenDownloads,
                )
            }
        }
    }

    @Test
    fun showsPodcasts_withFavoriteState() = runComposeUiTest {
        var toggled: Podcast? = null
        showContent(
            data = PagingData.from(listOf(podcast("1", "First show"), podcast("2", "Second show")), LoadStates(done, done, done)),
            favoriteIds = setOf("1"),
            onToggleFavorite = { toggled = it },
        )

        onNodeWithText("First show").assertIsDisplayed()
        onNodeWithText("Second show").assertIsDisplayed()
        onNodeWithContentDescription("Remove from favorites").assertIsDisplayed()
        onNodeWithContentDescription("Add to favorites").performClick()

        assertEquals("2", toggled?.id)
    }

    @Test
    fun refreshError_showsMessageAndRetry() = runComposeUiTest {
        val error = LoadState.Error(AppException(AppError.TooManyRequests))
        showContent(data = PagingData.from(emptyList(), LoadStates(error, done, done)))

        onNodeWithText("Too many requests", substring = true).assertIsDisplayed()
        onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun offlineWithDownloads_offersGoToDownloads() = runComposeUiTest {
        var opened = 0
        val error = LoadState.Error(AppException(AppError.Network))
        showContent(data = PagingData.from(emptyList(), LoadStates(error, done, done)), onOpenDownloads = { opened++ })

        onNodeWithText("Go to downloads").performClick()

        assertEquals(1, opened)
    }

    @Test
    fun emptyList_withDownloads_offersGoToDownloads() = runComposeUiTest {
        showContent(data = PagingData.from(emptyList(), LoadStates(done, done, done)), onOpenDownloads = {})
        onNodeWithText("Go to downloads").assertIsDisplayed()
    }

    @Test
    fun refreshError_withoutDownloads_hasNoDownloadsButton() = runComposeUiTest {
        val error = LoadState.Error(AppException(AppError.Network))
        showContent(data = PagingData.from(emptyList(), LoadStates(error, done, done)))
        onNodeWithText("Go to downloads").assertDoesNotExist()
    }

    @Test
    fun emptyList_showsEmptyMessage() = runComposeUiTest {
        showContent(data = PagingData.from(emptyList(), LoadStates(done, done, done)))

        onNodeWithText("No podcasts for these filters.").assertIsDisplayed()
    }

    @Test
    fun quotaRunningLow_showsBanner() = runComposeUiTest {
        showContent(
            data = PagingData.from(emptyList(), LoadStates(done, done, done)),
            quota = ApiQuota(used = 2100, freeQuota = 2500, nextBillingDate = null, plan = "FREE"),
        )

        onNodeWithText("Listen Notes quota is running low: 2100 of 2500 requests used.").assertIsDisplayed()
    }

    @Test
    fun activeGenre_showsChipThatOpensAndClears() = runComposeUiTest {
        var opened: FiltersTab? = null
        var cleared = false
        showContent(
            data = PagingData.from(emptyList(), LoadStates(done, done, done)),
            filters = FiltersUiState(
                genres = OptionsState.Loaded(listOf(Genre(133, "Comedy", 67))),
                filters = PodcastFilters(genreId = 133),
            ),
            onOpenFilters = { opened = it },
            onClearGenre = { cleared = true },
        )

        onNodeWithText("Comedy").performClick()
        onNodeWithContentDescription("Clear Comedy filter").performClick()

        assertEquals(FiltersTab.GENRE, opened)
        assertEquals(true, cleared)
    }

    private fun ComposeUiTest.manyPodcasts() =
        PagingData.from((1..40).map { podcast("$it", "Show $it") }, LoadStates(done, done, done))

    private val comedyFilter = FiltersUiState(
        genres = OptionsState.Loaded(listOf(Genre(133, "Comedy", 67))),
        filters = PodcastFilters(genreId = 133),
    )

    @Test
    fun filterChips_hideOnScrollDown_andShowOnScrollUp() = runComposeUiTest {
        showContent(data = manyPodcasts(), filters = comedyFilter)
        onNodeWithText("Comedy").assertIsDisplayed()

        onNodeWithTag(PODCASTS_LIST_TAG).performTouchInput { swipeUp() }
        onNodeWithText("Comedy").assertIsNotDisplayed()
        onNodeWithText("Show 1").assertIsNotDisplayed() // list really scrolled

        onNodeWithTag(PODCASTS_LIST_TAG).performTouchInput {
            swipeDown(startY = centerY, endY = centerY + 300f)
        }
        onNodeWithText("Comedy").assertIsDisplayed()
        onNodeWithText("Show 1").assertIsNotDisplayed() // header came back mid-list
    }

    @Test
    fun pinnedFilterChips_stayOnScroll() = runComposeUiTest {
        showContent(data = manyPodcasts(), filters = comedyFilter, filtersPinned = true)

        onNodeWithTag(PODCASTS_LIST_TAG).performTouchInput { swipeUp() }

        onNodeWithText("Comedy").assertIsDisplayed()
    }
}
