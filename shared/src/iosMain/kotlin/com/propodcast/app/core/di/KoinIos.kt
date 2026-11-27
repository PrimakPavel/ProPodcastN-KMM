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
import com.propodcast.app.core.download.DownloadProgressPresenter
import com.propodcast.app.widget.IosPlayerWidgetPublisher
import com.propodcast.app.widget.WidgetReloader

/**
 * iOS entry point, called from the SwiftUI app: `KoinIosKt.doInitKoinIos(analytics:crashlytics:widgetReloader:downloadProgress:)`.
 * [analytics] and [crashlytics] are the Swift Firebase bridges, or nil when the app has no GoogleService-Info.plist;
 * [widgetReloader] reloads the WidgetKit widget after the shared code publishes a new player state;
 * [downloadProgress] shows running downloads as Live Activities.
 */
@OptIn(ExperimentalNativeApi::class)
fun initKoinIos(
    analytics: AnalyticsTracker?,
    crashlytics: IosCrashlyticsBridge?,
    widgetReloader: WidgetReloader,
    downloadProgress: DownloadProgressPresenter,
): KoinApplication {
    val crashReporter = crashlytics?.let(::IosCrashReporter)
    setUpLogging(debug = Platform.isDebugBinary, crashReporter = crashReporter)
    crashReporter?.let(::reportUncaughtKotlinExceptions)
    return initKoin {
        modules(
            module {
                single<AnalyticsTracker> { analytics ?: NoOpAnalyticsTracker }
                single<DownloadProgressPresenter> { downloadProgress }
            },
        )
    }.also { app ->
        IosPlayerWidgetPublisher(controller = app.koin.get(), preferences = app.koin.get(), reloader = widgetReloader).start()
    }
}
