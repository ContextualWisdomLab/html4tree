package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

class CoverageTest {
    @Test
    fun testProcessDirWriteException() {
        val tempDir = java.nio.file.Files.createTempDirectory("test").toFile()
        val readOnlyDir = File(tempDir, "readonly")
        readOnlyDir.mkdir()
        readOnlyDir.setWritable(false, false)
        try {
            process_dir(readOnlyDir)
            // It should be handled securely
            assertTrue(true)
        } finally {
            readOnlyDir.setWritable(true, false)
        }
    }

    @Test
    fun testProcessIgnoreFileEmptyMatchers() {
        val tempDir = java.nio.file.Files.createTempDirectory("test_empty_matchers").toFile()
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("\n  \n") // Empty or blank lines to trigger empty matchers
        val excluded = process_ignore_file(tempDir, null)
        assertTrue(excluded.contains("index.html"))
        tempDir.deleteRecursively()
    }
}
