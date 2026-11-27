package com.propodcast.app.core.di

import com.propodcast.app.core.analytics.AnalyticsTracker
import com.propodcast.app.core.analytics.NoOpAnalyticsTracker
import com.propodcast.app.core.crash.IosCrashReporter
import com.propodcast.app.core.crash.IosCrashlyticsBridge
import com.propodcast.app.core.crash.reportUncaughtKotlinExceptions
import com.propodcast.app.core.crash.setUpLogging
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import org.koin.core.KoinApplication
import org.koin.dsl.module

/**
 * iOS entry point, called from the SwiftUI app: `KoinIosKt.doInitKoinIos(analytics:crashlytics:)`.
 * Both are the Swift Firebase bridges, or nil when the app has no GoogleService-Info.plist.
 */
@OptIn(ExperimentalNativeApi::class)
fun initKoinIos(analytics: AnalyticsTracker?, crashlytics: IosCrashlyticsBridge?): KoinApplication {
    val crashReporter = crashlytics?.let(::IosCrashReporter)
    setUpLogging(debug = Platform.isDebugBinary, crashReporter = crashReporter)
    crashReporter?.let(::reportUncaughtKotlinExceptions)
    return initKoin {
        modules(module { single<AnalyticsTracker> { analytics ?: NoOpAnalyticsTracker } })
    }
}
