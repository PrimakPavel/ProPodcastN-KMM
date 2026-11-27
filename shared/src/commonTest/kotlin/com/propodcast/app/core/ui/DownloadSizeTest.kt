package com.propodcast.app.core.ui

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.Test

class DownloadSizeTest {

    @Test
    fun estimate_isAboutOneMegabyteAMinute_roundedUpToTens() {
        assertNull(estimatedDownloadMb(0))
        assertEquals(10, estimatedDownloadMb(60)) // ~1 MB
        assertEquals(60, estimatedDownloadMb(56 * 60)) // ~54 MB
        assertEquals(70, estimatedDownloadMb(64 * 60)) // ~61 MB
        assertEquals(120, estimatedDownloadMb(2 * 3_600)) // ~115 MB
    }
}
