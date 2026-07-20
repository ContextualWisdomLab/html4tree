package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

class SentinelGlobTest {
    @Test
    fun testMalformedGlob() {
        val tempDir = File.createTempFile("test", "")
        tempDir.delete()
        tempDir.mkdir()
        val ignoreFile = File(tempDir, ".html4ignore")
        // No syntax prefix "glob:" - this throws pure IllegalArgumentException
        ignoreFile.writeText("invalid_glob\n")

        File(tempDir, "a").createNewFile()

        try {
            val result = process_ignore_file(tempDir, null)
            assertTrue(result.contains("index.html"))
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
