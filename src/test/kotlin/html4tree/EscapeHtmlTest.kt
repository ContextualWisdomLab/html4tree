package html4tree

import org.junit.Test
import kotlin.test.assertEquals

class EscapeHtmlTest {
    @Test
    fun testEscapeHtmlNoSpecial() {
        val s = "no special chars"
        assertEquals("no special chars", s.escapeHtml())
    }

    @Test
    fun testEscapeHtmlAllSpecial() {
        val s = "&<>\"'`"
        assertEquals("&amp;&lt;&gt;&quot;&#x27;&#x60;", s.escapeHtml())
    }

    @Test
    fun testEscapeHtmlMixed() {
        val s = "a&b<c>d\"e'f`g"
        assertEquals("a&amp;b&lt;c&gt;d&quot;e&#x27;f&#x60;g", s.escapeHtml())
    }
}
