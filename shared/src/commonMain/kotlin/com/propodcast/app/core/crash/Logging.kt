package com.propodcast.app.core.crash

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter

/**
 * Kermit setup, as Timber was in ProPodcastN: debug builds log everything to Logcat / the Xcode console;
 * release builds log nothing locally. With Firebase, warnings and errors also go to Crashlytics.
 */
fun setUpLogging(debug: Boolean, crashReporter: CrashReporter?) {
    Logger.setLogWriters(
        listOfNotNull(
            if (debug) platformLogWriter() else null,
            crashReporter?.let(::CrashReporterLogWriter),
        ),
    )
    Logger.setMinSeverity(if (debug) Severity.Verbose else Severity.Warn)
}
