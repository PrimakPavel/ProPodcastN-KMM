package com.propodcast.app.core.analytics

/** Used when Firebase is not configured (no google-services.json) and in tests. */
object NoOpAnalyticsTracker : AnalyticsTracker {
    override fun logScreen(screenName: String) = Unit
    override fun logEvent(name: String, params: Map<String, String>) = Unit
}
