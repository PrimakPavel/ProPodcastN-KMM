package com.propodcast.app.core.testing

import kotlin.time.Clock
import kotlin.time.Instant

/** A [Clock] tests move by hand. */
class TestClock(var nowMs: Long = 0) : Clock {
    override fun now(): Instant = Instant.fromEpochMilliseconds(nowMs)
}
