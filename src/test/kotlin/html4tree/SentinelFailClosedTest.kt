package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue
import kotlin.test.fail

class SentinelFailClosedTest {
    @Test
    fun testFailClosedWhenIgnoreFileNotReadable() {
        val tempDir = Files.createTempDirectory("test").toFile()
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("test.txt")
        ignoreFile.setReadable(false)

        try {
            process_ignore_file(tempDir, arrayOf(".html4ignore", "test.txt"))
            fail("Expected IgnoreFileReadException or similar")
        } catch(e: IgnoreFileReadException) {
            // Expected
        }
    }
}
