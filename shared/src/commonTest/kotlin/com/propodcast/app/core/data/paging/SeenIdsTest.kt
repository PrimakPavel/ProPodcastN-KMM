package com.propodcast.app.core.data.paging

import kotlin.test.assertEquals
import kotlin.test.Test

class SeenIdsTest {

    @Test
    fun keepNew_dropsRepeatsAcrossAndWithinCalls() {
        val seen = SeenIds<String> { it }

        assertEquals(listOf("a", "b"), seen.keepNew(listOf("a", "b", "a")))
        assertEquals(listOf("c"), seen.keepNew(listOf("b", "c")))
        assertEquals(emptyList<String>(), seen.keepNew(listOf("a", "c")))
    }
}
