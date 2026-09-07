package html4tree

import org.junit.Test
import org.junit.Assert.assertEquals

class HtmlEscapeTest {
    @Test
    fun testEscapeHtml_AsciiReserved() {
        assertEquals("&amp;", "&".escapeHtml())
        assertEquals("&lt;", "<".escapeHtml())
        assertEquals("&gt;", ">".escapeHtml())
        assertEquals("&quot;", "\"".escapeHtml())
        assertEquals("&#x27;", "'".escapeHtml())
        assertEquals("&#x60;", "`".escapeHtml())
    }

    @Test
    fun testEscapeHtml_NonAsciiBmp() {
        assertEquals("가나다", "가나다".escapeHtml())
        assertEquals("áéíóú", "áéíóú".escapeHtml())
        assertEquals("こんにちは", "こんにちは".escapeHtml())
    }

    @Test
    fun testEscapeHtml_SurrogatePairs() {
        // Emoji 🚀
        val emoji = "\uD83D\uDE80"
        assertEquals(emoji, emoji.escapeHtml())
    }

    @Test
    fun testEscapeHtml_EmptyAndLargeInputs() {
        assertEquals("", "".escapeHtml())
        val largeInput = "A".repeat(10000)
        assertEquals(largeInput, largeInput.escapeHtml())
    }

    @Test
    fun testEscapeHtml_AlreadyEscaped() {
        assertEquals("&amp;amp;", "&amp;".escapeHtml())
        assertEquals("&amp;lt;", "&lt;".escapeHtml())
    }
}
