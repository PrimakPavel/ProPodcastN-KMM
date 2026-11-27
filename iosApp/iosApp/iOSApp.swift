import SwiftUI
import Shared

/// Hands background download events to the shared downloads (iOS relaunches the app to deliver them).
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        handleEventsForBackgroundURLSession identifier: String,
        completionHandler: @escaping () -> Void
    ) {
        IosDownloadsRepository.companion.handleBackgroundEvents(
            identifier: identifier,
            completionHandler: completionHandler
        )
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init() {
        KoinKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
