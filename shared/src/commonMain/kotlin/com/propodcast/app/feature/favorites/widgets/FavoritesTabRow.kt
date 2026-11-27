package com.propodcast.app.feature.favorites.widgets

import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import com.propodcast.app.feature.favorites.FavoritesTab

/** Tabs with item counts, e.g. "Podcasts (3)"; the count is hidden while it is zero. */
@Composable
internal fun FavoritesTabRow(
    selected: FavoritesTab,
    counts: Map<FavoritesTab, Int>,
    onSelect: (FavoritesTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    PrimaryTabRow(selectedTabIndex = selected.ordinal, modifier = modifier) {
        FavoritesTab.entries.forEach { tab ->
            val label = stringResource(tab.labelRes)
            val count = counts[tab] ?: 0
            Tab(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                text = { Text(if (count > 0) "$label ($count)" else label) },
            )
        }
    }
}
