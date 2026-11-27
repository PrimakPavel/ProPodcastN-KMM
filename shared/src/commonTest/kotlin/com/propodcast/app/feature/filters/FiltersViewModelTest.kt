package com.propodcast.app.feature.filters

import app.cash.turbine.test
import com.propodcast.app.core.common.AppError
import com.propodcast.app.core.common.AppException
import com.propodcast.app.core.model.Genre
import com.propodcast.app.core.model.PodcastFilters
import com.propodcast.app.core.model.Region
import com.propodcast.app.core.testing.FakePodcastRepository
import com.propodcast.app.core.testing.FakeUserPreferencesRepository
import com.propodcast.app.core.testing.MainDispatcherRule
import com.propodcast.app.core.ui.OptionsState
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class FiltersViewModelTest {

    private val mainDispatcherRule = MainDispatcherRule()

    @BeforeTest
    fun setMainDispatcher() = mainDispatcherRule.start()

    @AfterTest
    fun resetMainDispatcher() = mainDispatcherRule.finish()

    private val comedy = Genre(133, "Comedy", 67)
    private val ukraine = Region("ua", "Ukraine")
    private val usa = Region("us", "United States")

    private val repository = FakePodcastRepository().apply {
        genresResult = Result.success(listOf(comedy))
        regionsResult = Result.success(listOf(ukraine, usa))
    }
    private val preferences = FakeUserPreferencesRepository()

    @Test
    fun loadsOptions_andResolvesSelectedNames() = runTest {
        preferences.filters.value = PodcastFilters(genreId = 133, regionCode = "ua")
        val viewModel = FiltersViewModel(repository, preferences)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(OptionsState.Loaded(listOf(comedy)), state.genres)
            assertEquals(comedy, state.selectedGenre)
            assertEquals(ukraine, state.selectedRegion)
            assertEquals("ua", state.effectiveRegionCode)
            assertTrue(state.hasActiveFilters)
        }
    }

    @Test
    fun noRegionFilter_meansApiDefaultRegion() = runTest {
        val viewModel = FiltersViewModel(repository, preferences)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals("us", state.effectiveRegionCode)
            assertNull(state.selectedRegion)
            assertFalse(state.hasActiveFilters)
        }
    }

    @Test
    fun selectRegion_defaultRegionClearsFilter() = runTest {
        val viewModel = FiltersViewModel(repository, preferences)

        viewModel.selectRegion("ua")
        assertEquals("ua", preferences.filters.value.regionCode)

        viewModel.selectRegion("us")
        assertNull(preferences.filters.value.regionCode)
    }

    @Test
    fun selectGenre_savesAndNullMeansAll() = runTest {
        val viewModel = FiltersViewModel(repository, preferences)

        viewModel.selectGenre(133)
        assertEquals(133, preferences.filters.value.genreId)

        viewModel.selectGenre(null)
        assertNull(preferences.filters.value.genreId)
    }

    @Test
    fun loadError_thenRetrySucceeds() = runTest {
        repository.genresResult = Result.failure(AppException(AppError.TooManyRequests))
        val viewModel = FiltersViewModel(repository, preferences)

        viewModel.uiState.test {
            assertEquals(OptionsState.Error(AppError.TooManyRequests), expectMostRecentItem().genres)

            repository.genresResult = Result.success(listOf(comedy))
            viewModel.loadGenres()
            assertEquals(OptionsState.Loaded(listOf(comedy)), expectMostRecentItem().genres)
        }
    }
}
