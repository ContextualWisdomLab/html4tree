package html4tree

import org.junit.Test
import kotlin.test.assertEquals

class BidiEscapeTest {
    @Test
    fun testBidiEscaping() {
        assertEquals("\\u202E", "\u202E".escapeHtml())
        assertEquals("\\u202A", "\u202A".escapeHtml())
        assertEquals("\\u202B", "\u202B".escapeHtml())
        assertEquals("\\u202C", "\u202C".escapeHtml())
        assertEquals("\\u202D", "\u202D".escapeHtml())
        assertEquals("\\u2066", "\u2066".escapeHtml())
        assertEquals("\\u2067", "\u2067".escapeHtml())
        assertEquals("\\u2068", "\u2068".escapeHtml())
        assertEquals("\\u2069", "\u2069".escapeHtml())
    }
}
