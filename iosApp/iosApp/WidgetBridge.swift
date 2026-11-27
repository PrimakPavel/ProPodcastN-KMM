import Shared
import WidgetKit

/// Lets the shared code reload the home screen widget (WidgetCenter is Swift-only).
final class WidgetCenterReloader: NSObject, WidgetReloader {
    func reloadPlayerWidget() {
        WidgetCenter.shared.reloadTimelines(ofKind: PlayerWidgetConfig.kind)
    }
}
