package com.propodcast.app.core.ui

import androidx.compose.runtime.Composable
import com.propodcast.app.resources.Res
import com.propodcast.app.resources.duration_hours_minutes
import com.propodcast.app.resources.duration_minutes
import org.jetbrains.compose.resources.stringResource

/**
 * A date in the user's locale and time zone, medium style, e.g. "Nov 14, 2016".
 * Platform formatters: kotlinx-datetime has no locale-aware formats.
 */
expect fun formatMediumDate(epochMs: Long): String

/** Publication date in the user's locale and time zone, e.g. "Nov 14, 2016". */
fun formatPubDate(epochMs: Long): String = formatMediumDate(epochMs)

/** "1 h 5 min" or "41 min"; empty when the duration is unknown. */
@Composable
fun formatDuration(seconds: Int): String {
    if (seconds <= 0) return ""
    val totalMinutes = (seconds + 30) / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        stringResource(Res.string.duration_hours_minutes, hours, minutes)
    } else {
        stringResource(Res.string.duration_minutes, minutes.coerceAtLeast(1))
    }
}
