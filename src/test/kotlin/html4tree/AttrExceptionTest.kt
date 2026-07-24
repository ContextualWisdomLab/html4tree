package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import java.nio.file.Files

class AttrExceptionTest {
    @Test
    fun testExceptionInReadAttributes() {
        val tempDir = Files.createTempDirectory("attr_test").toFile()
        try {
            val nonExistentFile = File(tempDir, "does_not_exist")

            // This will trigger the exception block in readAttributes
            process_dir(tempDir, setOf(), arrayOf(nonExistentFile))

            val indexHtml = File(tempDir, "index.html")
            assertTrue(indexHtml.exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testExceptionInNewDirectoryStream() {
        val tempDir = Files.createTempDirectory("stream_test").toFile()
        try {
            val subDir = File(tempDir, "subdir")
            subDir.mkdir()
            process_dir(tempDir, setOf(), arrayOf(subDir)) { _ ->
                throw java.io.IOException("Mock exception")
            }
            val indexHtml = File(tempDir, "index.html")
            assertTrue(indexHtml.exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
