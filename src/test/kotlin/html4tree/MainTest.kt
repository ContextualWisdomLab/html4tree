package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import java.io.File
import kotlin.test.assertTrue

class MainTest {
    @Test
    fun testEscapeHtml() {
        assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;", escapeHtml("<script>alert(1)</script>"))
        assertEquals("&amp;quot;test&amp;quot;", escapeHtml("&quot;test&quot;"))
        assertEquals("&quot;test&quot;", escapeHtml("\"test\""))
        assertEquals("&#x27;test&#x27;", escapeHtml("'test'"))
    }

    @Test
    fun testProcessDirWithMaliciousName() {
        val tempDir = java.nio.file.Files.createTempDirectory("html4tree_test").toFile()
        val maliciousDir = File(tempDir, "safe_name")
        maliciousDir.mkdir()
        val maliciousFile = File(maliciousDir, "unsafe_name_alert(1)")
        maliciousFile.createNewFile()

        process_dir(maliciousDir)

        val indexFile = File(maliciousDir, "index.html")
        assertTrue(indexFile.exists())

        val indexContent = indexFile.readText()
        assertTrue(indexContent.contains("unsafe_name_alert(1)"))
        assertTrue(indexContent.contains("href=\"./unsafe_name_alert%281%29\""))

        tempDir.deleteRecursively()
    }
}
