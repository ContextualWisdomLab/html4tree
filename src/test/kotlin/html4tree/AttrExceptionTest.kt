package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import java.nio.file.Files
import kotlin.test.assertFalse

class AttrExceptionTest {
    @Test
    fun testExceptionInReadAttributes() {
        val tempDir = Files.createTempDirectory("attr_test").toFile()
        try {
            val nonExistentFile = File(tempDir, "does_not_exist")

            // This will trigger the exception block in readAttributes
            process_dir(tempDir, setOf(), arrayOf(nonExistentFile))

            val indexHtml = File(tempDir, "index.html").readText()
            assertTrue(File(tempDir, "index.html").exists())
            assertFalse(indexHtml.contains("does_not_exist"), "File with unreadable attributes should be skipped for security")
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
