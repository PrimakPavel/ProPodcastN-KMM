package com.propodcast.app.feature.filters.widgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.filters_all_genres
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import org.jetbrains.compose.resources.stringResource
import com.propodcast.app.core.commonwidgets.OptionRow
import com.propodcast.app.core.model.Genre

/** "All genres" plus one radio row per genre. */
internal fun LazyListScope.genreOptions(genres: List<Genre>, selectedId: Int?, onSelect: (Int?) -> Unit) {
    item(key = "all") {
        OptionRow(label = stringResource(Res.string.filters_all_genres), selected = selectedId == null) { onSelect(null) }
    }
    items(genres, key = { it.id }) { genre ->
        OptionRow(label = genre.name, selected = genre.id == selectedId) { onSelect(genre.id) }
    }
}
