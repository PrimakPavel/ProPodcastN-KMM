package com.propodcast.app.core.ui

import androidx.compose.runtime.Composable
import com.propodcast.app.resources.Res
import com.propodcast.app.resources.download_size_mb
import com.propodcast.app.resources.download_size_small
import kotlin.math.ceil
import org.jetbrains.compose.resources.stringResource

/**
 * Rough download size from the duration: Listen Notes has no file sizes, most podcasts are ~128 kbit/s
 * (about 1 MB a minute). Rounded up to tens of MB; null when the duration is unknown.
 */
fun estimatedDownloadMb(durationSec: Int): Int? {
    if (durationSec <= 0) return null
    val megabytes = durationSec * TYPICAL_BITRATE_BPS / 8.0 / 1_000_000
    return (ceil(megabytes / 10) * 10).toInt()
}

/** "< 10 MB", "60 MB", or null when unknown. */
@Composable
fun estimatedDownloadSizeLabel(durationSec: Int): String? = estimatedDownloadMb(durationSec)?.let { mb ->
    if (mb <= SMALL_MB) stringResource(Res.string.download_size_small) else stringResource(Res.string.download_size_mb, mb)
}

/** A real file size in the platform's style, e.g. "62 MB". */
@Composable
expect fun fileSizeLabel(bytes: Long): String

private const val TYPICAL_BITRATE_BPS = 128_000
private const val SMALL_MB = 10
