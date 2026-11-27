package com.propodcast.app

import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformTest {
    @Test
    fun platformName_isNotBlank() {
        assertTrue(platformName().isNotBlank())
    }
}
