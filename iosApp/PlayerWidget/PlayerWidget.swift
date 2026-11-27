import AppIntents
import SwiftUI
import WidgetKit

/// Home screen widget with the current or last episode and play / pause, like the Android (Glance) widget.
@main
struct PlayerWidgetBundle: WidgetBundle {
    var body: some Widget {
        PlayerWidget()
    }
}

struct PlayerWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: PlayerWidgetConfig.kind, provider: PlayerProvider()) { entry in
            PlayerWidgetView(snapshot: entry.snapshot)
                .containerBackground(for: .widget) { Color(uiColor: .systemBackground) }
        }
        .configurationDisplayName("ProPodcast player")
        .description("The last episode with play / pause")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

struct PlayerEntry: TimelineEntry {
    let date: Date
    let snapshot: PlayerWidgetSnapshot
}

/// One entry, never refreshed on a schedule: the app reloads the widget when the episode or play state changes.
struct PlayerProvider: TimelineProvider {
    func placeholder(in context: Context) -> PlayerEntry {
        PlayerEntry(date: .now, snapshot: .empty)
    }

    func getSnapshot(in context: Context, completion: @escaping (PlayerEntry) -> Void) {
        completion(PlayerEntry(date: .now, snapshot: .load()))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<PlayerEntry>) -> Void) {
        completion(Timeline(entries: [PlayerEntry(date: .now, snapshot: .load())], policy: .never))
    }
}

struct PlayerWidgetView: View {
    let snapshot: PlayerWidgetSnapshot
    @Environment(\.widgetFamily) private var family

    var body: some View {
        if let title = snapshot.episodeTitle {
            if family == .systemSmall {
                VStack(alignment: .leading, spacing: 8) {
                    HStack(alignment: .top) {
                        Artwork(url: snapshot.artworkURL, size: 56)
                        Spacer()
                        PlayPauseButton(snapshot: snapshot)
                    }
                    Spacer(minLength: 0)
                    Titles(title: title, snapshot: snapshot, lines: 2)
                }
            } else {
                HStack(spacing: 12) {
                    Artwork(url: snapshot.artworkURL, size: 64)
                    Titles(title: title, snapshot: snapshot, lines: 2)
                    Spacer(minLength: 0)
                    PlayPauseButton(snapshot: snapshot)
                }
            }
        } else {
            Text(snapshot.emptyText)
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
        }
    }
}

private struct Titles: View {
    let title: String
    let snapshot: PlayerWidgetSnapshot
    let lines: Int

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(title)
                .font(.subheadline.weight(.medium))
                .lineLimit(lines)
            if snapshot.hasError {
                Text(snapshot.errorText).font(.caption).foregroundStyle(.red).lineLimit(1)
            } else if let podcast = snapshot.podcastTitle {
                Text(podcast).font(.caption).foregroundStyle(.secondary).lineLimit(1)
            }
        }
    }
}

private struct Artwork: View {
    let url: URL?
    let size: CGFloat

    var body: some View {
        Group {
            if let url, let image = UIImage(contentsOfFile: url.path) {
                Image(uiImage: image).resizable().scaledToFill()
            } else {
                Image(systemName: "mic.fill")
                    .font(.system(size: size * 0.4))
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color.appPurple)
            }
        }
        .frame(width: size, height: size)
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}

private struct PlayPauseButton: View {
    let snapshot: PlayerWidgetSnapshot

    var body: some View {
        Button(intent: TogglePlaybackIntent()) {
            Image(systemName: snapshot.isPlaying ? "pause.fill" : "play.fill")
                .font(.system(size: 18, weight: .semibold))
                .foregroundStyle(.white)
                .frame(width: 44, height: 44)
                .background(Circle().fill(Color.appPurple))
        }
        .buttonStyle(.plain)
        .accessibilityLabel(snapshot.isPlaying ? snapshot.pauseLabel : snapshot.playLabel)
    }
}

private extension Color {
    /// The app's primary colour (Purple40), as the Glance widget's play button.
    static let appPurple = Color(red: 0x66 / 255, green: 0x50 / 255, blue: 0xA4 / 255)
}
