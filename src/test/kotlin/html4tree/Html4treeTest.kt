package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class Html4treeTest {
    @Test
    fun testProcessDirEscapesMaliciousDirectoryNames() {
        val tempDir = createTempDir("html4treeTest")
        try {
            val maliciousDir = File(tempDir, "<script>alert(1)</script>")
            maliciousDir.mkdirs()

            val maliciousFile = File(maliciousDir, "test file\" onmouseover=\"alert(1)")
            maliciousFile.createNewFile()

            val normalDir = File(maliciousDir, "normal_dir")
            normalDir.mkdirs()

            process_dir(maliciousDir)

            val indexFile = File(maliciousDir, "index.html")
            assertTrue(indexFile.exists())

            val content = indexFile.readText()

            assertTrue("Top directory name not escaped in title", content.contains("&lt;script&gt;alert(1)&lt;/script&gt;") || content.contains("script&gt;"))
            assertTrue("Top directory name not escaped in h1", content.contains("&lt;script&gt;alert(1)&lt;/script&gt;") || content.contains("script&gt;"))

            assertTrue("Child file name not escaped in href", content.contains("href=\"./test file&quot; onmouseover=&quot;alert(1)\""))
            assertTrue("Child file name not escaped in text", content.contains("&rtrif; test file&quot; onmouseover=&quot;alert(1)</a>"))

            assertTrue("Normal dir not included correctly", content.contains("href=\"./normal_dir/\""))
            assertTrue("Normal dir not included correctly", content.contains("&#128193; normal_dir</a>"))

            assertFalse("Found unescaped script tag", content.contains("<script>alert(1)</script>"))
            assertFalse("Found unescaped quote", content.contains("href=\"./test file\" onmouseover=\"alert(1)"))
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
