package com.propodcast.app.feature.filters

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.filters_genre
import com.propodcast.app.resources.filters_region
import org.jetbrains.compose.resources.StringResource


/** Tabs of the filters sheet. */
enum class FiltersTab(val titleRes: StringResource) {
    GENRE(Res.string.filters_genre),
    REGION(Res.string.filters_region),
}
