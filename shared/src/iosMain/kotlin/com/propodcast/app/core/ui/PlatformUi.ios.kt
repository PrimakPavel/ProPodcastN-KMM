package com.propodcast.app.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSByteCountFormatter
import platform.Foundation.NSByteCountFormatterCountStyleFile
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

actual fun formatMediumDate(epochMs: Long): String {
    val formatter = NSDateFormatter().apply {
        dateStyle = NSDateFormatterMediumStyle
        timeStyle = NSDateFormatterNoStyle
    }
    return formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochMs / 1000.0))
}

@Composable
actual fun fileSizeLabel(bytes: Long): String =
    NSByteCountFormatter.stringFromByteCount(bytes, NSByteCountFormatterCountStyleFile)

@Composable
actual fun rememberLinkSharer(): LinkSharer = remember {
    LinkSharer { title, url, _ ->
        val presenter = topViewController() ?: return@LinkSharer
        val sheet = UIActivityViewController(activityItems = listOf(title, url), applicationActivities = null)
        // iPad shows the sheet as a popover, which needs an anchor.
        sheet.popoverPresentationController?.sourceView = presenter.view
        presenter.presentViewController(sheet, animated = true, completion = null)
    }
}

/** iOS has no notification permission step for this app: downloads show no notifications there. */
@Composable
actual fun rememberNotificationPermissionRequester(): (action: () -> Unit) -> Unit = remember { { action -> action() } }

private fun topViewController(): UIViewController? {
    val window = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .flatMap { it.windows.filterIsInstance<UIWindow>() }
        .firstOrNull { it.isKeyWindow() }
    var top = window?.rootViewController
    while (top?.presentedViewController != null) top = top.presentedViewController
    return top
}
