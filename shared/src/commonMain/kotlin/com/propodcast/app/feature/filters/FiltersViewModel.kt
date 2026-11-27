package com.propodcast.app.feature.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propodcast.app.core.common.toAppError
import com.propodcast.app.core.data.repository.PodcastRepository
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.model.Genre
import com.propodcast.app.core.model.Region
import com.propodcast.app.core.network.ListenNotesApi
import com.propodcast.app.core.ui.OptionsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Genre / region filters for best podcasts. Selections are saved immediately. */
class FiltersViewModel(
    private val podcastRepository: PodcastRepository,
    private val userPreferences: UserPreferencesRepository,
) : ViewModel() {

    private val genres = MutableStateFlow<OptionsState<Genre>>(OptionsState.Loading)
    private val regions = MutableStateFlow<OptionsState<Region>>(OptionsState.Loading)

    val uiState: StateFlow<FiltersUiState> =
        combine(genres, regions, userPreferences.filters) { genres, regions, filters ->
            FiltersUiState(genres = genres, regions = regions, filters = filters)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), FiltersUiState())

    init {
        loadGenres()
        loadRegions()
    }

    fun loadGenres() {
        genres.value = OptionsState.Loading
        viewModelScope.launch {
            genres.value = podcastRepository.genres().fold(
                onSuccess = { OptionsState.Loaded(it) },
                onFailure = { OptionsState.Error(it.toAppError()) },
            )
        }
    }

    fun loadRegions() {
        regions.value = OptionsState.Loading
        viewModelScope.launch {
            regions.value = podcastRepository.regions().fold(
                onSuccess = { OptionsState.Loaded(it) },
                onFailure = { OptionsState.Error(it.toAppError()) },
            )
        }
    }

    /** Null = all genres. */
    fun selectGenre(genreId: Int?) {
        viewModelScope.launch { userPreferences.setGenre(genreId) }
    }

    /** Choosing the API default region clears the filter, so "no filter" stays the default. */
    fun selectRegion(regionCode: String?) {
        val code = regionCode?.takeUnless { it == ListenNotesApi.DEFAULT_REGION }
        viewModelScope.launch { userPreferences.setRegion(code) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
