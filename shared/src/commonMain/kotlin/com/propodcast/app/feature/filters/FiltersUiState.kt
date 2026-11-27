package com.propodcast.app.feature.filters

import com.propodcast.app.core.model.Genre
import com.propodcast.app.core.model.PodcastFilters
import com.propodcast.app.core.model.Region
import com.propodcast.app.core.network.ListenNotesApi
import com.propodcast.app.core.ui.OptionsState

data class FiltersUiState(
    val genres: OptionsState<Genre> = OptionsState.Loading,
    val regions: OptionsState<Region> = OptionsState.Loading,
    val filters: PodcastFilters = PodcastFilters(),
) {
    /** Region the API actually uses: no region filter means [ListenNotesApi.DEFAULT_REGION]. */
    val effectiveRegionCode: String
        get() = filters.regionCode ?: ListenNotesApi.DEFAULT_REGION

    /** Selected genre, once genres are loaded. */
    val selectedGenre: Genre?
        get() = (genres as? OptionsState.Loaded)?.items?.firstOrNull { it.id == filters.genreId }

    /** Selected (non-default) region, once regions are loaded. */
    val selectedRegion: Region?
        get() = filters.regionCode?.let { code ->
            (regions as? OptionsState.Loaded)?.items?.firstOrNull { it.code == code }
        }

    val hasActiveFilters: Boolean
        get() = filters.genreId != null || filters.regionCode != null
}
