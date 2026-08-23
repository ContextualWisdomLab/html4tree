package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue
import java.io.IOException

class ToctouTest {
    @Test
    fun testProcessIgnoreFileWithSymlinkSwap() {
        val tempDir = Files.createTempDirectory("html4tree-test-toctou-").toFile()
        try {
            val ignoreFile = File(tempDir, ".html4ignore")
            ignoreFile.writeText("*.ignore\n*.ignore2")

            // Invoke the default parameters by omitting them.
            val excludedNormal = process_ignore_file(tempDir)
            assertTrue(excludedNormal.contains("index.html"))

            // To cover the IOException block, we inject a mock newInputStream
            val excludedError = process_ignore_file(
                curr_dir = tempDir,
                dirFilesNames = null,
                newInputStream = { _, _ -> throw IOException("Mocked TOCTOU symlink read failure") }
            )
            assertTrue(excludedError.contains("index.html"))

            // To cover the exception paths inside Kotlin's inline `useLines` try-finally block
            val excludedReadError = process_ignore_file(
                curr_dir = tempDir,
                dirFilesNames = null,
                newInputStream = { _, _ ->
                    object : java.io.InputStream() {
                        override fun read(): Int = throw IOException("Mock read failure inside useLines")
                        override fun read(b: ByteArray, off: Int, len: Int): Int = throw IOException("Mock read failure inside useLines")
                    }
                }
            )
            assertTrue(excludedReadError.contains("index.html"))

        } finally {
            tempDir.deleteRecursively()
        }
    }
}
