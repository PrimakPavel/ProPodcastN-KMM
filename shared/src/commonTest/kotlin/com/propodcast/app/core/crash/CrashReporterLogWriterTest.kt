package com.propodcast.app.core.crash

import co.touchlab.kermit.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CrashReporterLogWriterTest {

    private class RecordingReporter : CrashReporter {
        val lines = mutableListOf<String>()
        val exceptions = mutableListOf<Throwable>()
        override fun log(message: String) { lines += message }
        override fun recordException(throwable: Throwable) { exceptions += throwable }
    }

    private val reporter = RecordingReporter()
    private val writer = CrashReporterLogWriter(reporter)

    @Test
    fun onlyWarningsAndErrors_areLoggable() {
        assertFalse(writer.isLoggable("t", Severity.Info))
        assertTrue(writer.isLoggable("t", Severity.Warn))
        assertTrue(writer.isLoggable("t", Severity.Error))
    }

    @Test
    fun errorWithThrowable_isNonFatal_warningIsOnlyALine() {
        val boom = IllegalStateException("boom")
        writer.log(Severity.Warn, "slow", "Net", IllegalStateException("ignored"))
        writer.log(Severity.Error, "failed", "Player", boom)

        assertEquals(listOf("W/Net: slow", "E/Player: failed"), reporter.lines)
        assertEquals(listOf<Throwable>(boom), reporter.exceptions)
    }
}
