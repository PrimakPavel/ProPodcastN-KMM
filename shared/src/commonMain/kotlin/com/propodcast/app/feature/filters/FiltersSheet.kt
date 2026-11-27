package com.propodcast.app.feature.filters

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.filters_title
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import com.propodcast.app.core.commonwidgets.OptionsList
import com.propodcast.app.core.commonwidgets.SheetHeader
import com.propodcast.app.core.commonwidgets.rememberSheetCloser
import com.propodcast.app.feature.filters.widgets.genreOptions
import com.propodcast.app.feature.filters.widgets.regionOptions

/** Bottom sheet with Genre / Region tabs. A selection is applied right away; close with ✕ or a swipe down. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersSheet(
    state: FiltersUiState,
    onSelectGenre: (Int?) -> Unit,
    onSelectRegion: (String?) -> Unit,
    onRetryGenres: () -> Unit,
    onRetryRegions: () -> Unit,
    onDismiss: () -> Unit,
    initialTab: FiltersTab = FiltersTab.GENRE,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab.ordinal) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val close = rememberSheetCloser(sheetState, onDismiss)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column {
            SheetHeader(title = stringResource(Res.string.filters_title), onClose = close)
            // Transparent, so the tabs sit on the sheet's own colour instead of a band of `surface`.
            PrimaryTabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent) {
                FiltersTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab.ordinal,
                        onClick = { selectedTab = tab.ordinal },
                        text = { Text(stringResource(tab.titleRes)) },
                    )
                }
            }
            val listModifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp)
            when (FiltersTab.entries[selectedTab]) {
                FiltersTab.GENRE -> OptionsList(state.genres, onRetryGenres, listModifier) { genres ->
                    genreOptions(genres, state.filters.genreId, onSelectGenre)
                }
                FiltersTab.REGION -> OptionsList(state.regions, onRetryRegions, listModifier) { regions ->
                    regionOptions(regions, state.effectiveRegionCode, onSelectRegion)
                }
            }
        }
    }
}
