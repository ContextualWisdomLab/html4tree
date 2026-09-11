package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
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
}
