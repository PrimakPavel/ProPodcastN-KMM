package com.propodcast.app.core.crash

import kotlin.experimental.ExperimentalNativeApi

/** Firebase Crashlytics, implemented in Swift (the Firebase iOS SDK comes through Swift Package Manager). */
interface IosCrashlyticsBridge {
    fun log(message: String)

    /** A Kotlin exception as a Crashlytics exception model: its class, message and Kotlin stack frames. */
    fun record(name: String, reason: String, stackTrace: List<String>)
}

/** [CrashReporter] over the Swift [IosCrashlyticsBridge]. */
@OptIn(ExperimentalNativeApi::class)
class IosCrashReporter(private val bridge: IosCrashlyticsBridge) : CrashReporter {
    override fun log(message: String) = bridge.log(message)

    override fun recordException(throwable: Throwable) {
        bridge.record(
            name = throwable::class.qualifiedName ?: "Throwable",
            reason = throwable.message.orEmpty(),
            stackTrace = throwable.getStackTrace().toList(),
        )
    }
}

/**
 * Reports an uncaught Kotlin exception with its Kotlin stack before the app terminates: without this the crash
 * report shows only the native abort.
 */
@OptIn(ExperimentalNativeApi::class)
internal fun reportUncaughtKotlinExceptions(reporter: CrashReporter) {
    setUnhandledExceptionHook { throwable ->
        reporter.log("Uncaught Kotlin exception")
        reporter.recordException(throwable)
    }
}
