package com.propodcast.app.core.crash

/** Crash reporting (Firebase Crashlytics on both platforms). */
interface CrashReporter {
    /** A line attached to the next crash or non-fatal report. */
    fun log(message: String)

    /** A non-fatal issue: the app goes on, the report shows the exception and its stack. */
    fun recordException(throwable: Throwable)
}
