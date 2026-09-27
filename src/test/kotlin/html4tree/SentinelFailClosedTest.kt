package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import org.junit.Assume.assumeTrue
import org.junit.After
import kotlin.test.assertTrue
import kotlin.test.fail

class SentinelFailClosedTest {
    private var tempDir: File? = null

    @After
    fun tearDown() {
        tempDir?.deleteRecursively()
    }

    @Test
    fun testFailClosedWhenIgnoreFileNotReadable() {
        val dir = Files.createTempDirectory("test").toFile()
        tempDir = dir
        val ignoreFile = File(dir, ".html4ignore")
        ignoreFile.writeText("test.txt")
        ignoreFile.setReadable(false)

        // Root users or certain CI environments might still be able to read the file.
        assumeTrue("Could not make file unreadable for test environment", !ignoreFile.canRead())

        try {
            process_ignore_file(dir, arrayOf(".html4ignore", "test.txt"))
            fail("Expected IgnoreFileReadException or similar")
        } catch(e: IgnoreFileReadException) {
            // Expected
        }
    }
}
