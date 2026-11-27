import AppIntents
import Foundation
#if !WIDGET_EXTENSION
import Shared
#endif

/// Shared by the app and the widget extension (this folder belongs to both targets).
enum PlayerWidgetConfig {
    static let appGroup = "group.com.propodcast.app.kmm"
    static let kind = "PlayerWidget"
    static let snapshotKey = "playerWidget"
}

/// What the shared code publishes for the widget (IosWidgetSnapshot in Kotlin), as JSON in the App Group defaults.
struct PlayerWidgetSnapshot: Codable {
    var episodeTitle: String?
    var podcastTitle: String?
    var isPlaying: Bool
    var hasError: Bool
    var artworkFile: String?
    var emptyText: String
    var errorText: String
    var playLabel: String
    var pauseLabel: String

    /// Before the app has run once.
    static let empty = PlayerWidgetSnapshot(
        episodeTitle: nil, podcastTitle: nil, isPlaying: false, hasError: false, artworkFile: nil,
        emptyText: "Nothing played yet. Tap to find a podcast.",
        errorText: "This episode can’t be played.", playLabel: "Play", pauseLabel: "Pause"
    )

    static func load() -> PlayerWidgetSnapshot {
        guard
            let json = UserDefaults(suiteName: PlayerWidgetConfig.appGroup)?.string(forKey: PlayerWidgetConfig.snapshotKey),
            let snapshot = try? JSONDecoder().decode(PlayerWidgetSnapshot.self, from: Data(json.utf8))
        else { return .empty }
        return snapshot
    }

    var artworkURL: URL? {
        guard let artworkFile else { return nil }
        return FileManager.default
            .containerURL(forSecurityApplicationGroupIdentifier: PlayerWidgetConfig.appGroup)?
            .appendingPathComponent("widget/\(artworkFile)")
    }
}

/// The widget's play / pause. An AudioPlaybackIntent runs in the app's process (iOS starts it in the background
/// if needed), where the shared player lives; the extension only declares it.
struct TogglePlaybackIntent: AudioPlaybackIntent {
    static var title: LocalizedStringResource = "Play or pause"

    func perform() async throws -> some IntentResult {
        #if !WIDGET_EXTENSION
        await MainActor.run { IosWidgetActions.shared.togglePlayback() }
        #endif
        return .result()
    }
}
