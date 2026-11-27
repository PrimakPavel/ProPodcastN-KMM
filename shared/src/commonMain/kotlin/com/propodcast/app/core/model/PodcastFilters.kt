package com.propodcast.app.core.model

/** Filters for best podcasts (genre, region) and search (language). Null means "any". */
data class PodcastFilters(
    val genreId: Int? = null,
    val regionCode: String? = null,
    val language: String? = null,
)
