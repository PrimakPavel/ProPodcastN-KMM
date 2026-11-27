package com.propodcast.app.core.crash

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

/** [CrashReporter] on Firebase Crashlytics (Android SDK). */
class FirebaseCrashReporter(private val crashlytics: FirebaseCrashlytics) : CrashReporter {
    override fun log(message: String) = crashlytics.log(message)
    override fun recordException(throwable: Throwable) = crashlytics.recordException(throwable)
}

/** True when the build had google-services.json, so Firebase started with the app. */
fun isFirebaseConfigured(context: Context): Boolean = FirebaseApp.getApps(context).isNotEmpty()

/** Logging for Android: Logcat in debug, Crashlytics for warnings and errors when Firebase is configured. */
fun setUpAndroidLogging(context: Context, debug: Boolean) {
    setUpLogging(
        debug = debug,
        crashReporter = if (isFirebaseConfigured(context)) FirebaseCrashReporter(FirebaseCrashlytics.getInstance()) else null,
    )
}
