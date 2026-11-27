import ActivityKit
import Shared

/// Lets the shared downloads show their progress as Live Activities (ActivityKit is Swift-only).
final class LiveActivityDownloadPresenter: NSObject, DownloadProgressPresenter {
    private var activities: [String: Activity<DownloadActivityAttributes>] = [:]

    func show(episodeId: String, title: String, cancelLabel: String, progress: KotlinFloat?) {
        let state = DownloadActivityAttributes.ContentState(progress: progress.map { Double($0.floatValue) })
        if let activity = activities[episodeId] {
            Task { await activity.update(ActivityContent(state: state, staleDate: nil)) }
            return
        }
        guard ActivityAuthorizationInfo().areActivitiesEnabled else { return }
        let attributes = DownloadActivityAttributes(episodeId: episodeId, title: title, cancelLabel: cancelLabel)
        activities[episodeId] = try? Activity.request(
            attributes: attributes,
            content: ActivityContent(state: state, staleDate: nil)
        )
    }

    func hide(episodeId: String) {
        guard let activity = activities.removeValue(forKey: episodeId) else { return }
        Task { await activity.end(nil, dismissalPolicy: .immediate) }
    }
}
