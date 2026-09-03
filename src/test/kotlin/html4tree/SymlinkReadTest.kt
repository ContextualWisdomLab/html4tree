package html4tree

import org.junit.Test
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.UnsupportedOperationException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SymlinkReadTest {

    @Test
    fun preexistingSymlinkIgnoreFileIsNotRead() {
        val tempDir = Files.createTempDirectory("test-symlink-read-")
        try {
            val secretFile = tempDir.resolve("secret.txt")
            Files.write(secretFile, listOf("test.txt"))

            val symlinkFile = tempDir.resolve(".html4ignore")
            try {
                Files.createSymbolicLink(symlinkFile, secretFile)
            } catch (_: UnsupportedOperationException) {
                return
            }

            val excludeSet = process_ignore_file(tempDir.toFile(), arrayOf("test.txt"))

            assertFalse(excludeSet.contains("test.txt"))
            assertTrue(excludeSet.contains("index.html"))
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun openFailureAfterValidationFallsBackToDefaultExclusions() {
        val tempDir = Files.createTempDirectory("test-ignore-open-race-")
        try {
            Files.write(tempDir.resolve(".html4ignore"), listOf("test.txt"))

            val excludeSet = process_ignore_file_with_opener(
                tempDir.toFile(),
                arrayOf("test.txt")
            ) {
                throw IOException("simulated replacement before open")
            }

            assertFalse(excludeSet.contains("test.txt"))
            assertTrue(excludeSet.contains("index.html"))
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun normalIgnoreFileStillExcludesMatchingEntry() {
        val tempDir = Files.createTempDirectory("test-normal-ignore-")
        try {
            Files.write(tempDir.resolve(".html4ignore"), listOf("test.txt"))

            val excludeSet = process_ignore_file(tempDir.toFile(), arrayOf("test.txt", "keep.txt"))

            assertTrue(excludeSet.contains("test.txt"))
            assertFalse(excludeSet.contains("keep.txt"))
            assertTrue(excludeSet.contains("index.html"))
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun readFailureAfterSuccessfulOpenIsNotSwallowed() {
        val tempDir = Files.createTempDirectory("test-ignore-read-failure-")
        try {
            Files.write(tempDir.resolve(".html4ignore"), listOf("test.txt"))

            val error = assertFailsWith<IOException> {
                process_ignore_file_with_opener(
                    tempDir.toFile(),
                    arrayOf("test.txt")
                ) {
                    object : InputStream() {
                        override fun read(): Int {
                            throw IOException("simulated read failure")
                        }
                    }
                }
            }

            assertEquals("simulated read failure", error.message)
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
}
