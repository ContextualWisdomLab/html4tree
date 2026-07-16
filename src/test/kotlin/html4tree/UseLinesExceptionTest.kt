package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.nio.file.Files

class UseLinesExceptionTest {

    @Test
    fun testProcessIgnoreFileThrowsException() {
        val tempDir = Files.createTempDirectory("ignore_test").toFile()
        try {
            val ignoreFile = File(tempDir, ".html4ignore")
            ignoreFile.writeText("pattern1")

            // To hit the exception block, we pass a directory name that contains a null byte
            // causing `Paths.get(current)` inside the try block to throw `InvalidPathException`.
            val badNames = arrayOf("valid.txt", "bad\u0000name.txt")

            val excluded = process_ignore_file(tempDir, badNames)
            assertTrue(excluded.contains("index.html"))
            assertTrue(excluded.contains(".git"))

        } finally {
            tempDir.deleteRecursively()
        }
    }
}
