package com.propodcast.app.core.ui

import kotlin.math.roundToInt

/** Player clock: "4:05", "12:30" or "1:02:03". Negative values count as 0. */
fun formatPlaybackTime(ms: Long): String {
    val totalSec = ms.coerceAtLeast(0) / 1_000
    val hours = totalSec / 3_600
    val minutes = totalSec % 3_600 / 60
    val seconds = totalSec % 60
    return if (hours > 0) {
        "$hours:${minutes.twoDigits()}:${seconds.twoDigits()}"
    } else {
        "$minutes:${seconds.twoDigits()}"
    }
}

/** Playback speed label: "1×", "1.25×", "0.75×" (up to two decimals, no trailing zeros). */
fun formatSpeed(speed: Float): String {
    val hundredths = (speed * 100).roundToInt()
    val fraction = (hundredths % 100).toString().padStart(2, '0').trimEnd('0')
    return if (fraction.isEmpty()) "${hundredths / 100}×" else "${hundredths / 100}.$fraction×"
}

private fun Long.twoDigits() = toString().padStart(2, '0')
