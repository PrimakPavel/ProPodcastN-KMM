import ActivityKit
import AppIntents
import SwiftUI
import WidgetKit

/// Lock screen and Dynamic Island view of a running download.
struct DownloadActivityWidget: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: DownloadActivityAttributes.self) { context in
            DownloadLockScreenView(attributes: context.attributes, state: context.state)
                .padding(16)
                .activityBackgroundTint(Color(uiColor: .systemBackground))
        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    Image(systemName: "arrow.down.circle.fill").font(.title2).foregroundStyle(Color.appPurple)
                }
                DynamicIslandExpandedRegion(.trailing) {
                    CancelButton(attributes: context.attributes)
                }
                DynamicIslandExpandedRegion(.bottom) {
                    VStack(alignment: .leading, spacing: 6) {
                        Text(context.attributes.title).font(.subheadline).lineLimit(1)
                        DownloadProgress(state: context.state)
                    }
                }
            } compactLeading: {
                Image(systemName: "arrow.down.circle.fill").foregroundStyle(Color.appPurple)
            } compactTrailing: {
                Text(percent(context.state)).font(.caption2.monospacedDigit())
            } minimal: {
                Image(systemName: "arrow.down.circle.fill").foregroundStyle(Color.appPurple)
            }
        }
    }
}

private struct DownloadLockScreenView: View {
    let attributes: DownloadActivityAttributes
    let state: DownloadActivityAttributes.ContentState

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "arrow.down.circle.fill").font(.title).foregroundStyle(Color.appPurple)
            VStack(alignment: .leading, spacing: 6) {
                Text(attributes.title).font(.subheadline.weight(.medium)).lineLimit(1)
                DownloadProgress(state: state)
            }
            CancelButton(attributes: attributes)
        }
    }
}

private struct DownloadProgress: View {
    let state: DownloadActivityAttributes.ContentState

    var body: some View {
        if let progress = state.progress {
            ProgressView(value: progress).tint(Color.appPurple)
        } else {
            ProgressView().progressViewStyle(.linear).tint(Color.appPurple)
        }
    }
}

private struct CancelButton: View {
    let attributes: DownloadActivityAttributes

    var body: some View {
        Button(intent: CancelDownloadIntent(episodeId: attributes.episodeId)) {
            Image(systemName: "xmark")
                .font(.system(size: 14, weight: .semibold))
                .frame(width: 32, height: 32)
                .background(Circle().fill(Color.secondary.opacity(0.2)))
        }
        .buttonStyle(.plain)
        .accessibilityLabel(attributes.cancelLabel)
    }
}

private func percent(_ state: DownloadActivityAttributes.ContentState) -> String {
    guard let progress = state.progress else { return "…" }
    return "\(Int((progress * 100).rounded()))%"
}

private extension Color {
    static let appPurple = Color(red: 0x66 / 255, green: 0x50 / 255, blue: 0xA4 / 255)
}
