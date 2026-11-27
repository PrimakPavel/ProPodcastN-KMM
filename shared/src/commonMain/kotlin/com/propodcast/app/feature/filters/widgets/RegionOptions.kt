package com.propodcast.app.feature.filters.widgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.filters_default_region_suffix
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import org.jetbrains.compose.resources.stringResource
import com.propodcast.app.core.commonwidgets.OptionRow
import com.propodcast.app.core.model.Region
import com.propodcast.app.core.network.ListenNotesApi

/** One radio row per region; the API default region is labelled "(default)". */
internal fun LazyListScope.regionOptions(regions: List<Region>, selectedCode: String, onSelect: (String?) -> Unit) {
    items(regions, key = { it.code }) { region ->
        val label = if (region.code == ListenNotesApi.DEFAULT_REGION) {
            stringResource(Res.string.filters_default_region_suffix, region.name)
        } else {
            region.name
        }
        OptionRow(label = label, selected = region.code == selectedCode) { onSelect(region.code) }
    }
}
