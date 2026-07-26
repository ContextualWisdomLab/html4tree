package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue

class GlobExceptionTest {
    @Test
    fun testProcessIgnoreFileWithMalformedGlobPattern() {
        val tempDir = Files.createTempDirectory("globexc").toFile()
        try {
            val ignoreFile = File(tempDir, ".html4ignore")
            // A pattern like "[" will throw PatternSyntaxException (a subclass of IllegalArgumentException)
            ignoreFile.writeText("[\n")
            val excluded = process_ignore_file(tempDir, null)
            assertTrue(excluded.contains("index.html"))
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
