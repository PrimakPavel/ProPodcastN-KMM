package com.propodcast.app.core.analytics

/** Product analytics. No user-entered text (e.g. search queries) goes into events. */
interface AnalyticsTracker {
    fun logScreen(screenName: String)
    fun logEvent(name: String, params: Map<String, String> = emptyMap())
}
