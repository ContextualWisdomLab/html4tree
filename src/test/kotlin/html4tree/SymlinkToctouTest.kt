package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class SymlinkToctouTest {
    @Test
    fun testProcessIgnoreFileSymlinkSwap() {
        val tempDir = Files.createTempDirectory("test").toFile()
        try {
            val ignoreFile = File(tempDir, ".html4ignore")
            val longPattern = "a".repeat(101)
            ignoreFile.writeText("*.txt\n$longPattern")
            val excluded = process_ignore_file(tempDir)
            assertTrue(excluded.contains("index.html"))

            // Over 1000 lines
            ignoreFile.writeText((1..1005).joinToString("\n") { "*.txt" })
            process_ignore_file(tempDir)

            // Empty pattern
            ignoreFile.writeText("   \n*.txt")
            process_ignore_file(tempDir)

            // Invalid pattern
            ignoreFile.writeText("[invalid\n*.txt")
            process_ignore_file(tempDir)

        } finally {
            tempDir.deleteRecursively()
        }
    }
}
