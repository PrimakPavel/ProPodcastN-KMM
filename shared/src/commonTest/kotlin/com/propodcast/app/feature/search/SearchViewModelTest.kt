package com.propodcast.app.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.paging.testing.asSnapshot
import com.propodcast.app.core.common.AppError
import com.propodcast.app.core.common.AppException
import com.propodcast.app.core.testing.FakeFavoritesRepository
import com.propodcast.app.core.testing.FakePodcastRepository
import com.propodcast.app.core.testing.FakeUserPreferencesRepository
import com.propodcast.app.core.testing.MainDispatcherRule
import com.propodcast.app.core.testing.testPodcast
import com.propodcast.app.core.ui.OptionsState
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SearchViewModelTest {

    private val mainDispatcherRule = MainDispatcherRule()

    @BeforeTest
    fun setMainDispatcher() = mainDispatcherRule.start()

    @AfterTest
    fun resetMainDispatcher() = mainDispatcherRule.finish()

    private val repository = FakePodcastRepository().apply {
        searchResult = { query, language -> listOf(testPodcast("$query-$language")) }
        languagesResult = Result.success(listOf("Ukranian", "English"))
    }
    private val preferences = FakeUserPreferencesRepository()
    private val favorites = FakeFavoritesRepository()

    private fun viewModel(savedQuery: String? = null) = SearchViewModel(
        SavedStateHandle(listOfNotNull(savedQuery?.let { "query" to it }).toMap()),
        repository,
        preferences,
        favorites,
    )

    /** Keeps the (cachedIn) results flow collected, like the screen does. */
    private fun TestScope.collectResults(vm: SearchViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.results.collect {} }
    }

    @Test
    fun searchableQuery_needsThreeCharactersAfterTrim() {
        assertNull(SearchViewModel.searchableQuery(""))
        assertNull(SearchViewModel.searchableQuery(" st "))
        assertEquals("sta", SearchViewModel.searchableQuery(" sta "))
    }

    @Test
    fun shortQuery_sendsNoRequest() = runTest {
        val vm = viewModel().also { collectResults(it) }

        vm.onQueryChange("s")
        vm.onQueryChange("st")
        advanceTimeBy(SearchViewModel.QUERY_DEBOUNCE_MS * 2)

        assertEquals(emptyList<Pair<String, String?>>(), repository.searchRequests)
    }

    @Test
    fun typing_isDebounced_toOneRequest() = runTest {
        val vm = viewModel().also { collectResults(it) }

        listOf("sta", "star", "star ", "star w").forEach {
            vm.onQueryChange(it)
            advanceTimeBy(100)
        }
        advanceTimeBy(SearchViewModel.QUERY_DEBOUNCE_MS + 1)

        assertEquals<Any>(listOf("star w" to null), repository.searchRequests)
    }

    @Test
    fun trailingSpaces_doNotTriggerNewRequest() = runTest {
        val vm = viewModel().also { collectResults(it) }

        vm.onQueryChange("star")
        advanceTimeBy(SearchViewModel.QUERY_DEBOUNCE_MS + 1)
        vm.onQueryChange("star  ")
        advanceTimeBy(SearchViewModel.QUERY_DEBOUNCE_MS + 1)

        assertEquals<Any>(listOf("star" to null), repository.searchRequests)
    }

    @Test
    fun language_isAppliedAndChangesRestartSearch() = runTest {
        val vm = viewModel(savedQuery = "star").also { collectResults(it) }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.languageFilter.collect {} }
        advanceTimeBy(SearchViewModel.QUERY_DEBOUNCE_MS + 1)

        vm.selectLanguage("Ukranian")
        advanceTimeBy(1)

        assertEquals(listOf("star" to null, "star" to "Ukranian"), repository.searchRequests)
        assertEquals(listOf("star-Ukranian"), vm.results.asSnapshot().map { it.id })
        assertEquals("Ukranian", vm.languageFilter.value.selected)
    }

    @Test
    fun query_isRestoredFromSavedState() = runTest {
        assertEquals("star", viewModel(savedQuery = "star").query.value)
    }

    @Test
    fun languages_loadedSorted_andRetryAfterError() = runTest {
        repository.languagesResult = Result.failure(AppException(AppError.Network))
        val vm = viewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.languageFilter.collect {} }
        assertEquals(OptionsState.Error(AppError.Network), vm.languageFilter.value.languages)

        repository.languagesResult = Result.success(listOf("Ukranian", "English"))
        vm.loadLanguages()

        assertEquals(OptionsState.Loaded(listOf("English", "Ukranian")), vm.languageFilter.value.languages)
    }
}
