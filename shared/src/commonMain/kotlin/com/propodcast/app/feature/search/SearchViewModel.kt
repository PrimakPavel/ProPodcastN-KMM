package com.propodcast.app.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.propodcast.app.core.common.toAppError
import com.propodcast.app.core.data.repository.FavoritesRepository
import com.propodcast.app.core.data.repository.PodcastRepository
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.ui.OptionsState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Podcast search. A request goes out only for [MIN_QUERY_LENGTH]+ characters (trimmed) and after
 * [QUERY_DEBOUNCE_MS] without typing, so typing does not fire a request per key (the FREE plan
 * allows 2 search requests per second). The language filter is saved.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SearchViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val podcastRepository: PodcastRepository,
    private val userPreferences: UserPreferencesRepository,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    /** What the user typed; survives process death. */
    val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    private val selectedLanguage: Flow<String?> = userPreferences.filters.map { it.language }.distinctUntilChanged()
    private val languages = MutableStateFlow<OptionsState<String>>(OptionsState.Loading)

    val languageFilter: StateFlow<LanguageFilterState> =
        combine(selectedLanguage, languages) { selected, languages -> LanguageFilterState(selected, languages) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), LanguageFilterState())

    /** Empty while the trimmed query is shorter than [MIN_QUERY_LENGTH]; otherwise paged results. */
    val results: Flow<PagingData<Podcast>> =
        combine(query.debounce(QUERY_DEBOUNCE_MS).map(::searchableQuery), selectedLanguage) { query, language ->
            query to language
        }
            .distinctUntilChanged()
            .flatMapLatest { (query, language) ->
                if (query == null) flowOf(PagingData.empty()) else podcastRepository.searchPodcasts(query, language)
            }
            .cachedIn(viewModelScope)

    val favoriteIds: StateFlow<Set<String>> = favoritesRepository.favoritePodcastIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptySet())

    init {
        loadLanguages()
    }

    fun onQueryChange(value: String) {
        savedStateHandle[KEY_QUERY] = value
    }

    /** Null = any language. */
    fun selectLanguage(language: String?) {
        viewModelScope.launch { userPreferences.setLanguage(language) }
    }

    fun loadLanguages() {
        languages.value = OptionsState.Loading
        viewModelScope.launch {
            languages.value = podcastRepository.languages().fold(
                onSuccess = { OptionsState.Loaded(it.sorted()) },
                onFailure = { OptionsState.Error(it.toAppError()) },
            )
        }
    }

    fun toggleFavorite(podcast: Podcast) {
        val makeFavorite = podcast.id !in favoriteIds.value
        viewModelScope.launch { favoritesRepository.setPodcastFavorite(podcast, makeFavorite) }
    }

    companion object {
        const val QUERY_DEBOUNCE_MS = 500L
        const val MIN_QUERY_LENGTH = 3

        /** The trimmed query if it is long enough to search, otherwise null. */
        fun searchableQuery(query: String): String? = query.trim().takeIf { it.length >= MIN_QUERY_LENGTH }

        private const val KEY_QUERY = "query"
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
