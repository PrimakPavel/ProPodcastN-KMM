package com.propodcast.app.core.testing

import com.propodcast.app.core.analytics.AnalyticsTracker

/** Records analytics calls as "name {params}" / "screen name". */
class FakeAnalyticsTracker : AnalyticsTracker {
    val events = mutableListOf<String>()

    override fun logScreen(screenName: String) {
        events += "screen $screenName"
    }

    override fun logEvent(name: String, params: Map<String, String>) {
        events += if (params.isEmpty()) name else "$name $params"
    }
}
