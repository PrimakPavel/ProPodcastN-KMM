package com.propodcast.app.core.crash

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity

/**
 * Sends warnings and errors logged through Kermit to the [reporter]: as log lines attached to the next report,
 * and errors with a throwable as non-fatal issues. Crashes are reported by Crashlytics itself.
 */
class CrashReporterLogWriter(private val reporter: CrashReporter) : LogWriter() {

    override fun isLoggable(tag: String, severity: Severity): Boolean = severity >= Severity.Warn

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        reporter.log("${if (severity >= Severity.Error) "E" else "W"}/$tag: $message")
        if (throwable != null && severity >= Severity.Error) reporter.recordException(throwable)
    }
}
