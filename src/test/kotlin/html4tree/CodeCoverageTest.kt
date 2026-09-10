package html4tree

import org.junit.Test
import kotlin.test.assertEquals

class CodeCoverageTest {
    @Test
    fun testEscapeHtmlArrayMapCoverage() {
        // Create an array with characters covering all branches
        val sb = StringBuilder()
        // Characters to be escaped
        sb.append("&<>\"\'`")
        // Characters < 128 not to be escaped
        sb.append("A")
        // Character >= 128
        sb.append(128.toChar())

        val result = sb.toString().escapeHtml()
        assertEquals("&amp;&lt;&gt;&quot;&#x27;&#x60;A" + 128.toChar(), result)
    }
}
