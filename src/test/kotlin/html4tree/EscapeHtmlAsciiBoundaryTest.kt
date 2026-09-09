package html4tree

import org.junit.Test
import kotlin.test.assertEquals

class EscapeHtmlAsciiBoundaryTest {
    @Test
    fun nonAsciiCharactersPassThroughWhileAsciiMarkupStillEscapes() {
        assertEquals("한é", "한é".escapeHtml())
        assertEquals("한&amp;é", "한&é".escapeHtml())
    }
}
