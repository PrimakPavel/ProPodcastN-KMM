package com.propodcast.app.core.ui

import androidx.compose.ui.text.LinkAnnotation
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Guards what podcast descriptions rely on in HtmlConverter (same output on Android and iOS). */
class HtmlTextTest {

    @Test
    fun paragraphs_entities_andLinks() {
        val text = htmlToAnnotatedString(
            """<p>Tom &amp; Jerry&rsquo;s show.</p><p>Visit <a href="https://example.com/?a=1&amp;b=2">our site</a></p>""",
        )
        assertTrue(text.text.startsWith("Tom & Jerry’s show.\n"), text.text)
        assertTrue(text.text.trimEnd().endsWith("Visit our site"), text.text)
        val link = text.getLinkAnnotations(0, text.length).single()
        assertEquals("https://example.com/?a=1&b=2", (link.item as LinkAnnotation.Url).url)
        assertEquals("our site", text.text.substring(link.start, link.end))
    }
}
