package html4tree

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class IgnoreFileReadExceptionTest {
    private lateinit var tempDir: File

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("ignore-file-test").toFile()
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testProcessIgnoreFileThrowsExceptionWhenUnreadable() {
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("*.txt")
        ignoreFile.setReadable(false)

        var thrown = false
        try {
            process_ignore_file(tempDir, arrayOf("test.txt"))
        } catch (e: IgnoreFileReadException) {
            thrown = true
        } finally {
            ignoreFile.setReadable(true) // So it can be deleted
        }
        assertTrue("Expected IgnoreFileReadException to be thrown when file is unreadable", thrown)
    }

    @Test
    fun testProcessIgnoreFileThrowsExceptionOnIoError() {
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("*.txt")

        var thrown = false
        try {
            process_ignore_file(tempDir, arrayOf("test.txt")) { _, _ ->
                throw java.io.IOException("Simulated I/O Error")
            }
        } catch (e: IgnoreFileReadException) {
            thrown = true
        }
        assertTrue("Expected IgnoreFileReadException to be thrown on I/O error", thrown)
    }
}
