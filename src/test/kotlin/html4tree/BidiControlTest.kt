package html4tree

import org.junit.Test
import kotlin.test.assertEquals

class BidiControlTest {
    @Test
    fun testEscapeHtmlCoversAllUnicodeBidirectionalOrderingMarks() {
        assertEquals("\\u061C", "\u061C".escapeHtml())
        assertEquals("\\u200E", "\u200E".escapeHtml())
        assertEquals("\\u200F", "\u200F".escapeHtml())
    }
}
