package com.propodcast.app.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class EnglishPluralsTest {

    @Test
    fun onlyOneIsSingular() {
        assertEquals(1, englishPluralQuantity(1))
        // Numbers that are "one" in some languages (Ukrainian 21, 161; French 0) still get the plural form.
        listOf(0, 2, 5, 11, 21, 161, 391, 1483).forEach { count ->
            assertEquals(6, englishPluralQuantity(count), "count $count")
        }
    }
}
