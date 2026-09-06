package html4tree

import org.junit.Test
import kotlin.test.assertEquals

class EscapeHtmlLookupContractTest {
    @Test
    fun escapeHtmlPreservesProtectedMappingAcrossAsciiAndUnicode() {
        assertEquals("", "".escapeHtml())
        assertEquals("plain ASCII 123", "plain ASCII 123".escapeHtml())
        assertEquals("&amp;&lt;&gt;&quot;&#x27;&#x60;", "&<>\"'`".escapeHtml())
        assertEquals("한글 中文 日本語 العربية עברית", "한글 中文 日本語 العربية עברית".escapeHtml())
        assertEquals("A&amp;한글&lt;עברית&gt;Z", "A&한글<עברית>Z".escapeHtml())
    }
}
