package html4tree

import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class ToctouTest {
    @Test
    fun testProcessIgnoreFileWithSymlink() {
        val tempDir = Files.createTempDirectory("test-symlink").toFile()
        tempDir.deleteOnExit()

        val ignoreFile = File(tempDir, ".html4ignore")
        val realIgnore = Files.createTempFile("real", "ignore").toFile()
        realIgnore.writeText("*.txt")
        realIgnore.deleteOnExit()

        Files.createSymbolicLink(ignoreFile.toPath(), realIgnore.toPath())

        val excluded = process_ignore_file(tempDir)
        assertFalse(excluded.contains("*.txt"))
    }
}
