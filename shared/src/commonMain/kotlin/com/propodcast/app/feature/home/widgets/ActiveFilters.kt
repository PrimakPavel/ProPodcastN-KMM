package com.propodcast.app.feature.home.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.propodcast.app.feature.filters.FiltersTab
import com.propodcast.app.feature.filters.FiltersUiState

/** Clearable chips for the active genre / region filters; nothing when no filter is set. */
@Composable
internal fun ActiveFilters(
    state: FiltersUiState,
    onOpenFilters: (FiltersTab) -> Unit,
    onClearGenre: () -> Unit,
    onClearRegion: () -> Unit,
) {
    val genre = state.selectedGenre
    val region = state.selectedRegion
    if (genre == null && region == null) return
    // Chips are 32dp tall; without the 48dp minimum touch-target padding they sit right under the app bar.
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        LazyRow(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (genre != null) {
                item(key = "genre") {
                    ActiveFilterChip(genre.name, onClick = { onOpenFilters(FiltersTab.GENRE) }, onClear = onClearGenre)
                }
            }
            if (region != null) {
                item(key = "region") {
                    ActiveFilterChip(region.name, onClick = { onOpenFilters(FiltersTab.REGION) }, onClear = onClearRegion)
                }
            }
        }
    }
}
