package com.propodcast.app.core.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsTracker(private val analytics: FirebaseAnalytics) : AnalyticsTracker {

    /** Single-activity app: screen views come from navigation, not from activities. */
    override fun logScreen(screenName: String) {
        analytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
            },
        )
    }

    override fun logEvent(name: String, params: Map<String, String>) {
        analytics.logEvent(name, Bundle().apply { params.forEach { (key, value) -> putString(key, value) } })
    }
}
