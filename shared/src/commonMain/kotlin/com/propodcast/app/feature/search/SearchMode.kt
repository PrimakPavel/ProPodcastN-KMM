package com.propodcast.app.feature.search

/** What the search area shows for the current query. */
enum class SearchMode {
    /** Empty query: "Find podcasts by name…" hint. */
    START,

    /** 1–2 characters: "Type at least 3 characters" hint, no request. */
    TOO_SHORT,

    /** Searchable query: paged results. */
    RESULTS;

    companion object {
        fun of(query: String): SearchMode = when {
            query.isBlank() -> START
            SearchViewModel.searchableQuery(query) == null -> TOO_SHORT
            else -> RESULTS
        }
    }
}
