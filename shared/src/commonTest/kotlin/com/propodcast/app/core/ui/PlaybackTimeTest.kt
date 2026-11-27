package com.propodcast.app.core.ui

import kotlin.test.assertEquals
import kotlin.test.Test

class PlaybackTimeTest {

    @Test
    fun formatPlaybackTime_minutesAndHours() {
        assertEquals("0:00", formatPlaybackTime(0))
        assertEquals("0:00", formatPlaybackTime(-5_000))
        assertEquals("0:59", formatPlaybackTime(59_999))
        assertEquals("4:05", formatPlaybackTime(245_000))
        assertEquals("59:59", formatPlaybackTime(3_599_000))
        assertEquals("1:00:00", formatPlaybackTime(3_600_000))
        assertEquals("1:02:03", formatPlaybackTime(3_723_000))
    }

    @Test
    fun formatSpeed_dropsTrailingZeros() {
        assertEquals("1×", formatSpeed(1f))
        assertEquals("1.25×", formatSpeed(1.25f))
        assertEquals("1.5×", formatSpeed(1.5f))
        assertEquals("0.75×", formatSpeed(0.75f))
        assertEquals("2×", formatSpeed(2f))
    }
}
