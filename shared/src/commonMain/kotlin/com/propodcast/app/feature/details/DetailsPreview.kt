package com.propodcast.app.feature.details

import com.propodcast.app.core.model.Podcast

/** What the details screen already knows before loading: from the tapped list item. */
data class DetailsPreview(
    val title: String? = null,
    val artworkUrl: String? = null,
    val artworkKey: String? = null,
)

/** Just enough of a [Podcast] for the header while the real details load (artwork and title). */
internal fun DetailsPreview.toPodcast() = Podcast(
    id = "",
    title = title.orEmpty(),
    publisher = "",
    imageUrl = null,
    thumbnailUrl = artworkUrl,
    description = "",
    totalEpisodes = 0,
    listennotesUrl = null,
)
