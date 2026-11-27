import FirebaseAnalytics
import FirebaseCore
import FirebaseCrashlytics
import Shared

/// Firebase starts only when the app has GoogleService-Info.plist (it is not in git), as on Android.
enum FirebaseSetup {
    static func configureIfPresent() -> Bool {
        guard Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil else { return false }
        FirebaseApp.configure()
        return true
    }
}

/// The shared AnalyticsTracker on Firebase Analytics.
final class FirebaseAnalyticsTracker: NSObject, AnalyticsTracker {
    /// Screen views come from navigation, not from view controllers.
    func logScreen(screenName: String) {
        Analytics.logEvent(
            AnalyticsEventScreenView,
            parameters: [AnalyticsParameterScreenName: screenName, AnalyticsParameterScreenClass: screenName]
        )
    }

    func logEvent(name: String, params: [String: String]) {
        Analytics.logEvent(name, parameters: params)
    }
}

/// Crashlytics for the shared code: log lines, non-fatal and uncaught Kotlin exceptions with Kotlin frames.
final class FirebaseCrashlyticsBridge: NSObject, IosCrashlyticsBridge {
    func log(message: String) {
        Crashlytics.crashlytics().log(message)
    }

    func record(name: String, reason: String, stackTrace: [String]) {
        let model = ExceptionModel(name: name, reason: reason)
        model.stackTrace = stackTrace.map { StackFrame(symbol: $0, file: "", line: 0) }
        Crashlytics.crashlytics().record(exceptionModel: model)
    }
}
