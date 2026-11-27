package com.propodcast.app.feature.details

import androidx.lifecycle.SavedStateHandle
import com.propodcast.app.core.common.AppError
import com.propodcast.app.core.common.AppException
import com.propodcast.app.core.model.PodcastDetails
import com.propodcast.app.core.testing.FakeFavoritesRepository
import com.propodcast.app.core.testing.FakePodcastRepository
import com.propodcast.app.core.testing.MainDispatcherRule
import com.propodcast.app.core.testing.testEpisode
import com.propodcast.app.core.testing.testPodcast
import com.propodcast.app.navigation.PodcastDetailsRoute
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PodcastDetailsViewModelTest {

    private val mainDispatcherRule = MainDispatcherRule()

    @BeforeTest
    fun setMainDispatcher() = mainDispatcherRule.start()

    @AfterTest
    fun resetMainDispatcher() = mainDispatcherRule.finish()

    private val podcast = testPodcast("p1")
    private val repository = FakePodcastRepository()
    private val favorites = FakeFavoritesRepository()

    private fun details(vararg episodeIds: String, next: Long?) =
        Result.success(PodcastDetails(podcast, episodeIds.map { testEpisode(it) }, next))

    private fun viewModel() = PodcastDetailsViewModel(
        SavedStateHandle(mapOf(PodcastDetailsRoute.ARG_PODCAST_ID to "p1")),
        repository,
        favorites,
    )

    /** uiState is WhileSubscribed: keep a collector so combine() runs. */
    private fun TestScope.subscribe(vm: PodcastDetailsViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
    }

    private val PodcastDetailsViewModel.loaded get() = uiState.value.content as DetailsContent.Loaded

    @Test
    fun load_showsPodcastEpisodesAndRecommendations() = runTest {
        repository.detailsResult = { _, _ -> details("e1", "e2", next = 100L) }
        repository.recommendationsResult = Result.success(listOf(testPodcast("r1"), podcast))
        val vm = viewModel().also { subscribe(it) }

        assertEquals(listOf("e1", "e2"), vm.loaded.episodes.map { it.id })
        assertTrue(vm.loaded.canLoadMore)
        assertEquals(listOf("r1"), vm.uiState.value.recommendations.map { it.id }, "recommendations exclude the podcast itself")
        assertEquals<Any>(listOf("p1" to null), repository.detailsRequests)
    }

    @Test
    fun loadError_thenRetry() = runTest {
        repository.detailsResult = { _, _ -> Result.failure(AppException(AppError.NotFound)) }
        val vm = viewModel().also { subscribe(it) }
        assertEquals(DetailsContent.Error(AppError.NotFound), vm.uiState.value.content)

        repository.detailsResult = { _, _ -> details("e1", next = null) }
        vm.load()

        assertEquals(listOf("e1"), vm.loaded.episodes.map { it.id })
        assertFalse(vm.loaded.canLoadMore)
    }

    @Test
    fun loadMore_appendsNextPage_withPubDateKey() = runTest {
        repository.detailsResult = { _, next ->
            if (next == null) details("e1", "e2", next = 100L) else details("e3", next = null)
        }
        val vm = viewModel().also { subscribe(it) }

        vm.loadMoreEpisodes()

        assertEquals(listOf("e1", "e2", "e3"), vm.loaded.episodes.map { it.id })
        assertFalse(vm.loaded.canLoadMore)
        assertEquals(listOf("p1" to null, "p1" to 100L), repository.detailsRequests)
    }

    @Test
    fun loadMore_samePageAgain_stopsLoadingMore() = runTest {
        // Mock server: every page has the same episodes and the same next_episode_pub_date.
        repository.detailsResult = { _, _ -> details("e1", "e2", next = 100L) }
        val vm = viewModel().also { subscribe(it) }

        vm.loadMoreEpisodes()

        assertEquals(listOf("e1", "e2"), vm.loaded.episodes.map { it.id })
        assertFalse(vm.loaded.canLoadMore)
    }

    @Test
    fun loadMoreError_keepsEpisodes_andRetryWorks() = runTest {
        var failNext = true
        repository.detailsResult = { _, next ->
            when {
                next == null -> details("e1", next = 100L)
                failNext -> Result.failure(AppException(AppError.TooManyRequests))
                else -> details("e2", next = null)
            }
        }
        val vm = viewModel().also { subscribe(it) }

        vm.loadMoreEpisodes()
        assertEquals(AppError.TooManyRequests, vm.loaded.loadMoreError)
        assertEquals(listOf("e1"), vm.loaded.episodes.map { it.id })

        failNext = false
        vm.loadMoreEpisodes()
        assertNull(vm.loaded.loadMoreError)
        assertEquals(listOf("e1", "e2"), vm.loaded.episodes.map { it.id })
    }

    @Test
    fun recommendationsFailure_hidesSection() = runTest {
        repository.detailsResult = { _, _ -> details("e1", next = null) }
        repository.recommendationsResult = Result.failure(AppException(AppError.Network))
        val vm = viewModel().also { subscribe(it) }

        assertTrue(vm.uiState.value.recommendations.isEmpty())
        assertTrue(vm.uiState.value.content is DetailsContent.Loaded)
    }

    @Test
    fun favorites_toggleForPodcastAndEpisode() = runTest {
        repository.detailsResult = { _, _ -> details("e1", next = null) }
        val vm = viewModel().also { subscribe(it) }

        vm.togglePodcastFavorite()
        vm.toggleEpisodeFavorite(testEpisode("e1"))
        assertTrue(vm.uiState.value.isPodcastFavorite)
        assertEquals(setOf("e1"), vm.uiState.value.favoriteEpisodeIds)

        vm.togglePodcastFavorite()
        vm.toggleEpisodeFavorite(testEpisode("e1"))
        assertFalse(vm.uiState.value.isPodcastFavorite)
        assertEquals(emptySet<String>(), vm.uiState.value.favoriteEpisodeIds)
    }
}
