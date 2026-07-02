package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class SentinelTest {

    @Test
    fun testIgnoreFileIsDirectory() {
        val tempDir = Files.createTempDirectory("html4tree-test-sentinel").toFile()
        try {
            val ignoreDir = File(tempDir, ".html4ignore")
            ignoreDir.mkdir() // Create a directory instead of a file

            val testFile = File(tempDir, "test.txt")
            testFile.createNewFile()

            // Should not crash, and test.txt shouldn't be excluded
            val excluded = process_ignore_file(tempDir)
            assertFalse(excluded.contains("test.txt"))
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testIgnoreFileIsTooLarge() {
        val tempDir = Files.createTempDirectory("html4tree-test-sentinel").toFile()
        try {
            val ignoreFile = File(tempDir, ".html4ignore")
            val chars = ByteArray(1024 * 1024 + 10) { 'a'.toByte() }
            ignoreFile.writeBytes(chars) // Create file slightly larger than 1MB

            val testFile = File(tempDir, "test.txt")
            testFile.createNewFile()

            // Should not crash, and test.txt shouldn't be excluded since the file is ignored
            val excluded = process_ignore_file(tempDir)
            assertFalse(excluded.contains("test.txt"))
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
