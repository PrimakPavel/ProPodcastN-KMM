package com.propodcast.app.core.analytics

/** Event and parameter names, kept in one place so reports stay consistent. */
object AnalyticsEvents {
    const val PLAY_EPISODE = "play_episode"
    const val DOWNLOAD_EPISODE = "download_episode"
    const val ADD_FAVORITE = "add_favorite"
    const val SEARCH = "search"

    const val PARAM_SOURCE = "source"
    const val PARAM_TYPE = "type"
    const val PARAM_LANGUAGE_FILTER = "language_filter"

    const val SOURCE_DOWNLOAD = "download"
    const val SOURCE_STREAM = "stream"
    const val TYPE_PODCAST = "podcast"
    const val TYPE_EPISODE = "episode"
}
