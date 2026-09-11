package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BidiIsolationSecurityTest {
    @Test
    fun embeddedBidiControlsCannotTerminateGeneratedIsolation() {
        val directory = Files.createTempDirectory("html4tree-bidi-").toFile()
        try {
            val maliciousName = "report\u2069\u202Ecod.exe"
            process_dir(directory, emptySet(), arrayOf(File(directory, maliciousName)))

            val html = File(directory, "index.html").readText()
            val visibleName = "report\\u2069\\u202Ecod.exe"

            assertFalse(html.contains(maliciousName))
            assertTrue(html.contains("href=\"./report%E2%81%A9%E2%80%AEcod.exe\""))
            assertTrue(html.contains("title=\"&#x2068;${visibleName}&#x2069; 파일\""))
            assertTrue(html.contains(">${visibleName}</span>"))
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun allSupportedDirectionalControlsAreRenderedAsVisibleEscapes() {
        val controls = listOf(
            '\u061C' to "\\u061C",
            '\u200E' to "\\u200E",
            '\u200F' to "\\u200F",
            '\u202A' to "\\u202A",
            '\u202B' to "\\u202B",
            '\u202C' to "\\u202C",
            '\u202D' to "\\u202D",
            '\u202E' to "\\u202E",
            '\u2066' to "\\u2066",
            '\u2067' to "\\u2067",
            '\u2068' to "\\u2068",
            '\u2069' to "\\u2069"
        )
        val input = controls.joinToString(separator = "") { it.first.toString() }
        val expected = controls.joinToString(separator = "") { it.second }

        assertEquals(expected, input.escapeHtml())
    }
}
