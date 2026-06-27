package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class StringAndHelpTest {

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;", "&".escapeHtml())
        assertEquals("&lt;", "<".escapeHtml())
        assertEquals("&gt;", ">".escapeHtml())
        assertEquals("&quot;", "\"".escapeHtml())
        assertEquals("&#x27;", "'".escapeHtml())
        assertEquals("&lt;b&gt;hello &amp; world&lt;/b&gt;", "<b>hello & world</b>".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("hello%20world", "hello world".urlEncodePath())
        assertEquals("test%2Bplus", "test+plus".urlEncodePath())
    }

    @Test
    fun testHelp() {
        val originalOut = System.out
        val outContent = ByteArrayOutputStream()
        System.setOut(PrintStream(outContent))

        try {
            help()
            assertEquals("ERROR: help has not been written yet!\n", outContent.toString())
        } finally {
            System.setOut(originalOut)
        }
    }
}
