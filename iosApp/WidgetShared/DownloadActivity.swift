import ActivityKit
import AppIntents
import Foundation
#if !WIDGET_EXTENSION
import Shared
#endif

/// A running episode download as a Live Activity (lock screen and Dynamic Island): iOS has no ongoing progress
/// notifications, this is its equivalent of the Android download notification. Shared by the app (starts and
/// updates it) and the widget extension (draws it).
struct DownloadActivityAttributes: ActivityAttributes {
    struct ContentState: Codable, Hashable {
        /// 0...1, or nil while the size is unknown.
        var progress: Double?
    }

    var episodeId: String
    var title: String
    var cancelLabel: String
}

/// The Live Activity's Cancel button. A LiveActivityIntent runs in the app's process, where the downloads are.
struct CancelDownloadIntent: LiveActivityIntent {
    static var title: LocalizedStringResource = "Cancel download"

    @Parameter(title: "Episode")
    var episodeId: String

    init() {}

    init(episodeId: String) {
        self.episodeId = episodeId
    }

    func perform() async throws -> some IntentResult {
        #if !WIDGET_EXTENSION
        let id = episodeId
        await MainActor.run { IosDownloadActions.shared.cancel(episodeId: id) }
        #endif
        return .result()
    }
}
