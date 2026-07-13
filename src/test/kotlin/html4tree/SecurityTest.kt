package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.nio.file.Files

class SecurityTest {
    @Test
    fun testSensitiveFilesCaseInsensitive() {
        val tempDir = Files.createTempDirectory("sectest").toFile()
        try {
            val file1 = File(tempDir, "ID_RSA")
            file1.createNewFile()

            val excluded = process_ignore_file(tempDir, null)
            assertTrue("ID_RSA should be excluded", excluded.contains("ID_RSA"))

            process_dir(tempDir)

            val indexContent = File(tempDir, "index.html").readText()
            assertFalse("ID_RSA should be excluded from index.html", indexContent.contains("ID_RSA"))
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
